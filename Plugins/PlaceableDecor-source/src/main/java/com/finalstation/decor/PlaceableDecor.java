package com.finalstation.decor;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.entity.EntityBreakByEntityEvent;
import io.papermc.paper.event.entity.EntityBreakEvent;
import io.papermc.paper.event.player.PlayerPickEntityEvent;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Shelf;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class PlaceableDecor extends JavaPlugin implements Listener {
    private NamespacedKey itemKey,
            variantKey,
            styleKey,
            parentKey,
            displayKey,
            hitboxKey,
            orientationKey;
    private NativeCushions nativeCushions;
    private SpawnProtection protection;
    private List<Variant> variants = List.of();
    private Map<MatcherKey, Variant> variantLookup = Map.of();
    private Set<Material> variantMaterials = Set.of();
    private Pending pending;
    private long preventedVanillaPlacements;

    private record MatcherKey(Material material, String model, String name) {}

    record Variant(
            String id,
            Material material,
            String model,
            String name,
            String dataKey,
            String dataValue,
            double[] style) {}

    private static final class Pending {
        final Player player;
        final ItemStack item;
        final Variant variant;
        EntityPlaceEvent event;

        Pending(Player player, ItemStack item, Variant variant) {
            this.player = player;
            this.item = item.asOne();
            this.variant = variant;
        }
    }

    @Override
    public void onEnable() {
        var previousPlugin = getServer().getPluginManager().getPlugin("PikminDecor");
        if (previousPlugin != null && previousPlugin.isEnabled()) {
            getLogger().severe("Remove the old PikminDecor JAR before installing PlaceableDecor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Preserve the original namespace so all placed decorations survive the plugin rename.
        itemKey = new NamespacedKey("pikmindecor", "original_item");
        variantKey = new NamespacedKey("pikmindecor", "variant");
        styleKey = new NamespacedKey("pikmindecor", "style");
        parentKey = new NamespacedKey("pikmindecor", "parent");
        displayKey = new NamespacedKey("pikmindecor", "display");
        hitboxKey = new NamespacedKey("pikmindecor", "hitbox");
        orientationKey = new NamespacedKey("pikmindecor", "orientation_version");
        var legacyConfig =
                getDataFolder().toPath().resolveSibling("PikminDecor").resolve("config.yml");
        var currentConfig = getDataFolder().toPath().resolve("config.yml");
        try {
            if (!Files.exists(currentConfig) && Files.isRegularFile(legacyConfig)) {
                Files.createDirectories(getDataFolder().toPath());
                Files.copy(legacyConfig, currentConfig);
                getLogger().info("Imported existing PikminDecor config; original file retained.");
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot import existing decor configuration", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        if (!getConfig().contains("config-version", true)) {
            if (getConfig().getDouble("defaults.yaw-offset", 180) == 180)
                getConfig().set("defaults.yaw-offset", 0.0);
            getConfig().set("config-version", 2);
            saveConfig();
        }
        try {
            nativeCushions = new NativeCushions();
            loadVariants();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            getLogger()
                    .log(
                            Level.SEVERE,
                            "Cannot initialize PlaceableDecor; no items have been changed.",
                            e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        protection = new SpawnProtection(this);
        getServer().getPluginManager().registerEvents(this, this);
        // Wait until all plugins have enabled and all entity UUIDs are registered.
        getServer()
                .getScheduler()
                .runTask(
                        this,
                        () ->
                                Bukkit.getWorlds()
                                        .forEach(
                                                world -> {
                                                    repair(
                                                            List.copyOf(
                                                                    world.getEntitiesByClasses(
                                                                            Cushion.class,
                                                                            ItemDisplay.class,
                                                                            Interaction.class)));
                                                }));
        getLogger()
                .info(
                        "Loaded "
                                + variants.size()
                                + " decor variants; native Paper 26.3 cushion placement enabled.");
    }

    private void loadVariants() {
        ConfigurationSection section = getConfig().getConfigurationSection("variants");
        if (section == null || section.getKeys(false).isEmpty())
            throw new IllegalArgumentException("No variants configured");
        ConfigurationSection defaults = getConfig().getConfigurationSection("defaults");
        List<Variant> loaded = new ArrayList<>();
        var matchers = new HashSet<String>();
        for (String id : section.getKeys(false)) {
            ConfigurationSection c = section.getConfigurationSection(id);
            if (c == null)
                throw new IllegalArgumentException("Expected a section for variant " + id);
            Material material = Material.matchMaterial(c.getString("material", "FLINT"));
            String model = c.getString("item-model", "");
            if (material == null
                    || !material.isItem()
                    || material.isAir()
                    || NamespacedKey.fromString(model) == null) {
                throw new IllegalArgumentException("Invalid material or item-model for " + id);
            }
            String key = c.getString("custom-data-key", "");
            String value = c.getString("custom-data-value", "");
            String name = c.getString("custom-name");
            if (key.isEmpty() != value.isEmpty())
                throw new IllegalArgumentException("Set both custom-data fields for " + id);
            String matcher = material.name() + ":" + model + ":" + name;
            if (!matchers.add(matcher))
                throw new IllegalArgumentException(
                        "Duplicate material/item-model/name matcher for " + id);
            String[] fields = {"scale", "offset-y", "yaw-offset", "hitbox-width", "hitbox-height"};
            double[] fallback = {1.0, 0.5, 0, 0.45, 0.8};
            double[] style = new double[fields.length];
            for (int i = 0; i < fields.length; i++) {
                style[i] =
                        c.getDouble(
                                fields[i],
                                defaults == null
                                        ? fallback[i]
                                        : defaults.getDouble(fields[i], fallback[i]));
                if (!Double.isFinite(style[i]) || (Math.abs(style[i]) > 64 && i != 2)) {
                    throw new IllegalArgumentException("Invalid " + fields[i] + " for " + id);
                }
            }
            if (style[0] <= 0 || style[3] <= 0 || style[4] <= 0 || style[3] > 1) {
                throw new IllegalArgumentException(
                        "Scale/height must be positive; hitbox width must be 0..1 for " + id);
            }
            loaded.add(new Variant(id, material, model, name, key, value, style));
        }
        Map<MatcherKey, Variant> lookup = new HashMap<>();
        Set<Material> materials = new HashSet<>();
        for (Variant variant : loaded) {
            lookup.put(new MatcherKey(variant.material, variant.model, variant.name), variant);
            materials.add(variant.material);
        }
        variants = List.copyOf(loaded);
        variantLookup = Map.copyOf(lookup);
        variantMaterials = Set.copyOf(materials);
    }

    private Variant match(ItemStack item) throws ReflectiveOperationException {
        if (item == null || !variantMaterials.contains(item.getType())) return null;
        var model = item.getData(DataComponentTypes.ITEM_MODEL);
        String modelKey = model == null ? item.getType().getKey().asString() : model.asString();
        var nameComponent = item.getData(DataComponentTypes.CUSTOM_NAME);
        String name =
                nameComponent == null
                        ? null
                        : PlainTextComponentSerializer.plainText().serialize(nameComponent);
        Variant exact = variantLookup.get(new MatcherKey(item.getType(), modelKey, name));
        if (matchesData(item, exact)) return exact;
        Variant generic =
                name == null
                        ? null
                        : variantLookup.get(new MatcherKey(item.getType(), modelKey, null));
        return matchesData(item, generic) ? generic : null;
    }

    private boolean matchesData(ItemStack item, Variant variant)
            throws ReflectiveOperationException {
        return variant != null
                && (variant.dataKey.isEmpty()
                        || nativeCushions
                                .customString(item, variant.dataKey)
                                .equals(variant.dataValue));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getHand() == null
                || event.useItemInHand() == Event.Result.DENY) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR
                || player.getGameMode() == GameMode.ADVENTURE) return;
        try {
            Variant variant = match(event.getItem());
            if (variant == null) return;
            if (!player.isSneaking() && event.useInteractedBlock() != Event.Result.DENY) {
                var block = event.getClickedBlock();
                // Shelf slots are on the front; its top is still a decoration placement surface.
                if (block.getBlockData() instanceof Shelf shelf) {
                    if (event.getBlockFace() == shelf.getFacing()) return;
                } else if (block.getType().isInteractable()
                        && !(block.getBlockData() instanceof Stairs)
                        && !(block.getBlockData() instanceof Fence)) {
                    // Use containers, doors and buttons normally. Sneak to place instead.
                    // Fences (leash handling) and stairs also report interactable, but a hat
                    // should place on them rather than fall through to vanilla pumpkin placement.
                    return;
                }
            }
            // Don't open a trapdoor/chest while placing a decoration on it.
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            if (pending != null) return;
            Location hit = event.getInteractionPoint();
            if (hit == null)
                return; // Native player block interactions include the precise hit position.
            Pending attempt = new Pending(player, event.getItem(), variant);
            pending = attempt;
            boolean success;
            try {
                success =
                        nativeCushions.place(
                                player,
                                event.getHand(),
                                event.getClickedBlock(),
                                event.getBlockFace(),
                                hit.toVector());
            } finally {
                pending = null;
            }
            if (!success || attempt.event == null || attempt.event.isCancelled()) return;
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack held = player.getInventory().getItem(event.getHand());
                held.setAmount(held.getAmount() - 1);
            }
            Cushion cushion = (Cushion) attempt.event.getEntity();
            if (cushion.isValid()) ensureParts(cushion);
            player.swingHand(event.getHand());
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, "Native Pikmin placement failed", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventVanillaBlockPlacement(BlockPlaceEvent event) {
        try {
            if (match(event.getItemInHand()) != null) {
                // A declined block interaction must never turn a configured hat into a pumpkin.
                // Vanilla rolls back the block and item use when this event is cancelled.
                event.setCancelled(true);
                preventedVanillaPlacements++;
            }
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, "Cannot check decorative block item", e);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void prepareNativePlacement(EntityPlaceEvent event) {
        if (pending == null
                || event.getPlayer() != pending.player
                || !(event.getEntity() instanceof Cushion cushion)) return;
        pending.event = event;
        var data = cushion.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.BYTE_ARRAY, pending.item.serializeAsBytes());
        data.set(variantKey, PersistentDataType.STRING, pending.variant.id);
        data.set(
                styleKey,
                PersistentDataType.LIST.doubles(),
                java.util.Arrays.stream(pending.variant.style).boxed().toList());
        data.set(orientationKey, PersistentDataType.INTEGER, 1);
        hideBacking(cushion);
        cushion.setPersistent(true);
        Location at = cushion.getLocation();
        cushion.setRotation(
                facingYaw(at, pending.player.getLocation()) + (float) pending.variant.style[2], 0);
    }

    static float facingYaw(Location at, Location player) {
        double dx = player.getX() - at.getX(), dz = player.getZ() - at.getZ();
        return Math.abs(dx) + Math.abs(dz) < 1.0e-8
                ? player.getYaw() + 180
                : (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void protectPlacement(EntityPlaceEvent event) {
        if (isPikmin(event.getEntity())
                && protection.denied(event.getPlayer(), event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void protectBreaking(EntityBreakEvent event) {
        if (!isPikmin(event.getEntity())) return;
        if (event instanceof EntityBreakByEntityEvent byEntity) {
            Entity remover = byEntity.getRemover();
            Player player =
                    remover instanceof Player p
                            ? p
                            : remover instanceof Projectile p
                                            && p.getShooter() instanceof Player shooter
                                    ? shooter
                                    : null;
            if (protection.denied(player, event.getEntity().getLocation()))
                event.setCancelled(true);
        } else if (event.getCause() == EntityBreakEvent.RemoveCause.EXPLOSION
                && protection.denied(null, event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
        // Physics is left to vanilla: if an operator removes the support at spawn it must drop too.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction)
                || !(event.getDamager() instanceof Player player)) return;
        Cushion cushion = parent(event.getEntity());
        if (cushion == null) return;
        event.setCancelled(true);
        if (protection.denied(player, cushion.getLocation())) return;
        try {
            // Run the real cushion damage path, including permissions, break events, sound and
            // drops.
            nativeCushions.punch(cushion, player);
        } catch (ReflectiveOperationException e) {
            getLogger().log(Level.SEVERE, "Native Pikmin breaking failed", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(EntityDropItemEvent event) {
        ItemStack original = original(event.getEntity());
        if (original != null) event.getItemDrop().setItemStack(original);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (isPikmin(event.getRightClicked()) || parent(event.getRightClicked()) != null)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMount(EntityMountEvent event) {
        if (isPikmin(event.getMount())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPick(PlayerPickEntityEvent event) {
        Entity entity = event.getEntity();
        ItemStack original = original(isPikmin(entity) ? entity : parent(entity));
        if (original == null) return;
        // The pick event's mutable result is used by vanilla's inventory handling.
        ItemStack result = event.getItem();
        result.setType(original.getType());
        result.setItemMeta(original.getItemMeta());
        result.setAmount(1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRemove(EntityRemoveEvent event) {
        if (event.getCause() == EntityRemoveEvent.Cause.UNLOAD || !isPikmin(event.getEntity()))
            return;
        removePart(event.getEntity(), displayKey);
        removePart(event.getEntity(), hitboxKey);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoad(EntitiesLoadEvent event) {
        List<Entity> entities =
                event.getEntities().stream()
                        .filter(
                                e ->
                                        isPikmin(e)
                                                || ((e instanceof ItemDisplay
                                                                || e instanceof Interaction)
                                                        && e.getPersistentDataContainer()
                                                                .has(parentKey)))
                        .toList();
        if (entities.isEmpty()) return;
        // Hide loaded backings before they can be sent to clients; UUID repair waits a tick.
        for (Entity entity : entities) {
            if (isPikmin(entity)) hideBacking((Cushion) entity);
        }
        getServer().getScheduler().runTask(this, () -> repair(entities));
    }

    private boolean isPikmin(Entity entity) {
        return entity instanceof Cushion
                && entity.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE_ARRAY);
    }

    private ItemStack original(Entity entity) {
        if (!isPikmin(entity)) return null;
        byte[] bytes =
                entity.getPersistentDataContainer().get(itemKey, PersistentDataType.BYTE_ARRAY);
        return ItemStack.deserializeBytes(bytes).asOne();
    }

    private Entity linked(Entity entity, NamespacedKey key) {
        String id = entity.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) return null;
        try {
            return Bukkit.getEntity(UUID.fromString(id));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Cushion parent(Entity entity) {
        Entity parent = linked(entity, parentKey);
        return isPikmin(parent) ? (Cushion) parent : null;
    }

    private void removePart(Entity cushion, NamespacedKey key) {
        Entity part = linked(cushion, key);
        if (part != null
                && cushion.getUniqueId()
                        .toString()
                        .equals(
                                part.getPersistentDataContainer()
                                        .get(parentKey, PersistentDataType.STRING))) part.remove();
    }

    private void repair(List<Entity> entities) {
        // All three entities share their X/Z origin, so they always load in the same chunk.
        for (Entity entity : entities) {
            if (entity.isValid() && isPikmin(entity)) ensureParts((Cushion) entity);
        }
        for (Entity entity : entities) {
            if (!entity.isValid() || !entity.getPersistentDataContainer().has(parentKey)) continue;
            Cushion cushion = parent(entity);
            NamespacedKey key = entity instanceof ItemDisplay ? displayKey : hitboxKey;
            if (cushion == null || linked(cushion, key) != entity) entity.remove();
        }
    }

    private void ensureParts(Cushion cushion) {
        hideBacking(cushion);
        if (!cushion.getPersistentDataContainer().has(orientationKey)) {
            // Correct decorations created with the reversed 1.0.0 item-display facing basis, once
            // only.
            cushion.setRotation(cushion.getYaw() - 180, 0);
            cushion.getPersistentDataContainer().set(orientationKey, PersistentDataType.INTEGER, 1);
        }
        List<Double> savedStyle =
                cushion.getPersistentDataContainer()
                        .get(styleKey, PersistentDataType.LIST.doubles());
        if (savedStyle == null || savedStyle.size() != 5) return;
        double[] style = savedStyle.stream().mapToDouble(Double::doubleValue).toArray();
        Location at = cushion.getLocation();
        if (!(linked(cushion, displayKey) instanceof ItemDisplay)) {
            ItemDisplay display =
                    at.getWorld()
                            .spawn(
                                    at,
                                    ItemDisplay.class,
                                    entity -> {
                                        entity.setBillboard(Display.Billboard.FIXED);
                                        entity.setItemStack(original(cushion));
                                        entity.setItemDisplayTransform(
                                                ItemDisplay.ItemDisplayTransform.NONE);
                                        entity.setTransformation(
                                                new Transformation(
                                                        new Vector3f(0, (float) style[1], 0),
                                                        new Quaternionf(),
                                                        new Vector3f((float) style[0]),
                                                        new Quaternionf()));
                                        entity.setPersistent(true);
                                        entity.getPersistentDataContainer()
                                                .set(
                                                        parentKey,
                                                        PersistentDataType.STRING,
                                                        cushion.getUniqueId().toString());
                                    });
            cushion.getPersistentDataContainer()
                    .set(displayKey, PersistentDataType.STRING, display.getUniqueId().toString());
        }
        if (linked(cushion, displayKey) instanceof ItemDisplay display) {
            display.setBillboard(Display.Billboard.FIXED);
            display.setRotation(at.getYaw(), 0);
        }
        if (!(linked(cushion, hitboxKey) instanceof Interaction)) {
            Interaction hitbox =
                    at.getWorld()
                            .spawn(
                                    at,
                                    Interaction.class,
                                    entity -> {
                                        entity.setInteractionWidth((float) style[3]);
                                        entity.setInteractionHeight((float) style[4]);
                                        entity.setResponsive(false);
                                        entity.setPersistent(true);
                                        entity.getPersistentDataContainer()
                                                .set(
                                                        parentKey,
                                                        PersistentDataType.STRING,
                                                        cushion.getUniqueId().toString());
                                    });
            cushion.getPersistentDataContainer()
                    .set(hitboxKey, PersistentDataType.STRING, hitbox.getUniqueId().toString());
        }
    }

    private void hideBacking(Cushion cushion) {
        // The cushion client renderer does not reliably honor the invisible metadata flag.
        // Exclude the backing from player tracking entirely; the separate Interaction remains
        // targetable.
        cushion.setVisibleByDefault(false);
        cushion.setInvisible(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("finalstation.decor.admin")
                && !sender.hasPermission("finalstation.pikmin.admin")) return true;
        if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            long decorations =
                    Bukkit.getWorlds().stream()
                            .flatMap(w -> w.getEntitiesByClass(Cushion.class).stream())
                            .filter(this::isPikmin)
                            .count();
            long tasks =
                    Bukkit.getScheduler().getPendingTasks().stream()
                            .filter(t -> t.getOwner() == this)
                            .count();
            double[] tps = getServer().getTPS();
            sender.sendMessage(
                    String.format(
                            Locale.ROOT,
                            "§aPlaceableDecor %s: %d loaded decorations, %d configured variants.",
                            getPluginMeta().getVersion(),
                            decorations,
                            variants.size()));
            sender.sendMessage(
                    String.format(
                            Locale.ROOT,
                            "§7Server TPS (1/5/15m): %.2f / %.2f / %.2f; average tick: %.2f ms.",
                            Math.min(20, tps[0]),
                            Math.min(20, tps[1]),
                            Math.min(20, tps[2]),
                            getServer().getAverageTickTime()));
            sender.sendMessage(
                    "§7Own pending one-shot tasks: "
                            + tasks
                            + "; repeating tasks: 0; blocked vanilla fallbacks: "
                            + preventedVanillaPlacements);
            return true;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) return false;
        try {
            reloadConfig();
            loadVariants();
            sender.sendMessage("§aLoaded " + variants.size() + " decor variants.");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(
                    "§cInvalid config: " + e.getMessage() + ". Previous variants remain active.");
        }
        return true;
    }
}
