package com.finalstation.decor.tests;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Cushion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

/** Run ONLY in the disposable localhost test server; never install this on the real server. */
public final class IntegrationChecks extends JavaPlugin implements Listener {
    private JavaPlugin decor;
    private ServerPlayer fake;
    private org.bukkit.World world;
    private boolean denyPlacement;
    private int assertions;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler()
                .runTaskLater(
                        this,
                        () -> {
                            try {
                                runChecks();
                            } catch (Throwable e) {
                                getLogger()
                                        .log(
                                                java.util.logging.Level.SEVERE,
                                                "INTEGRATION CHECK FAILED",
                                                e);
                                Bukkit.shutdown();
                            }
                        },
                        10);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void deny(EntityPlaceEvent event) {
        if (denyPlacement && event.getEntity() instanceof Cushion) event.setCancelled(true);
    }

    private void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
        assertions++;
        getLogger().info("PASS " + description);
    }

    private ItemStack item(String id) {
        return Bukkit.getItemFactory()
                .createItemStack(
                        "minecraft:flint[minecraft:item_model=\"seedling:pikmin/"
                                + id
                                + "\",minecraft:custom_name={text:\"Test"
                                + " collectible\"},minecraft:custom_data={seedling_pikmin:\""
                                + id
                                + "\"}]");
    }

    private Cushion place(String id, int x, String data, double height, EquipmentSlot hand) {
        Block block = world.getBlockAt(x, 10, 128);
        block.setBlockData(Bukkit.createBlockData(data), false);
        fake.getBukkitEntity()
                .setSneaking(
                        block.getType().isInteractable()
                                && !(block.getBlockData()
                                        instanceof org.bukkit.block.data.type.Stairs)
                                && !(block.getBlockData()
                                        instanceof org.bukkit.block.data.type.Shelf));
        fake.setPos(x + 2, 11, 130);
        ItemStack stack = item(id);
        stack.setAmount(3);
        fake.getBukkitEntity().getInventory().setItem(hand, stack);
        PlayerInteractEvent event =
                new PlayerInteractEvent(
                        fake.getBukkitEntity(),
                        Action.RIGHT_CLICK_BLOCK,
                        stack,
                        block,
                        BlockFace.UP,
                        hand,
                        new Vector(0.21, height, 0.77));
        Bukkit.getPluginManager().callEvent(event);
        return world
                .getNearbyEntities(
                        new org.bukkit.Location(world, x + .5, 10 + height, 128.5), .2, .2, .2)
                .stream()
                .filter(Cushion.class::isInstance)
                .map(Cushion.class::cast)
                .findFirst()
                .orElse(null);
    }

    private List<Entity> parts(Cushion cushion) {
        String parent = cushion.getUniqueId().toString();
        return world.getEntities().stream()
                .filter(
                        e ->
                                parent.equals(
                                        e.getPersistentDataContainer()
                                                .get(
                                                        new NamespacedKey("pikmindecor", "parent"),
                                                        PersistentDataType.STRING)))
                .toList();
    }

    private void punch(Cushion cushion) {
        Interaction hitbox =
                (Interaction)
                        parts(cushion).stream()
                                .filter(Interaction.class::isInstance)
                                .findFirst()
                                .orElseThrow();
        // Exercise the real native player attack path, not a manually constructed Bukkit event.
        fake.attack(((CraftEntity) hitbox).getHandle());
    }

    private void runChecks() throws Exception {
        decor = (JavaPlugin) Bukkit.getPluginManager().getPlugin("PlaceableDecor");
        check(decor != null && decor.isEnabled(), "plugin enabled on Paper 26.3 build 145");
        if (getConfig().getBoolean("verify-rename", false)) {
            check(
                    decor.getDataFolder().getName().equals("PlaceableDecor"),
                    "plugin uses renamed data directory");
            check(
                    "kept".equals(decor.getConfig().getString("rename-migration-test")),
                    "custom config survives automatic rename migration");
            var legacy =
                    decor.getDataFolder()
                            .toPath()
                            .resolveSibling("PikminDecor")
                            .resolve("config.yml");
            check(
                    java.nio.file.Files.mismatch(
                                    legacy, decor.getDataFolder().toPath().resolve("config.yml"))
                            == -1,
                    "legacy config copied byte-for-byte with original retained");
        }
        world = Bukkit.getWorlds().getFirst();
        world.getChunkAt(8, 8).addPluginChunkTicket(this);
        var server = ((CraftServer) Bukkit.getServer()).getServer();
        var profile = new GameProfile(UUID.randomUUID(), "PikminTest");
        fake =
                new ServerPlayer(
                        server,
                        ((CraftWorld) world).getHandle(),
                        profile,
                        ClientInformation.createDefault());
        fake.connection =
                new ServerGamePacketListenerImpl(
                        server,
                        new Connection(PacketFlow.SERVERBOUND),
                        fake,
                        CommonListenerCookie.createInitial(profile, false));
        fake.getBukkitEntity().setGameMode(GameMode.SURVIVAL);
        if (getConfig().getBoolean("hat-checks-only", false)) {
            runHatChecks();
            return;
        }
        String restartId = getConfig().getString("restart-fixture");
        if (restartId != null) {
            Cushion saved = (Cushion) Bukkit.getEntity(UUID.fromString(restartId));
            check(saved != null && saved.isValid(), "placed Pikmin survives full server restart");
            check(
                    !fake.getBukkitEntity().canSee(saved),
                    "backing excluded from player visibility after restart");
            check(parts(saved).size() == 2, "restart keeps exactly one model and hitbox");
            double expectedYaw =
                    getConfig().getDouble("restart-yaw")
                            - (getConfig().getBoolean("fixed-facing", false) ? 0 : 180);
            check(
                    saved.isInvisible()
                            && Math.abs(Math.IEEEremainder(saved.getYaw() - expectedYaw, 360))
                                    < .01,
                    "restart retains invisibility and facing");
            check(
                    parts(saved).stream()
                            .filter(ItemDisplay.class::isInstance)
                            .map(ItemDisplay.class::cast)
                            .allMatch(d -> d.getItemStack().isSimilar(item("purple_spooky"))),
                    "restart retains original collectible");
            // Move the fixture away from the spawn ticket before exercising chunk unloading.
            world.getBlockAt(1600, 10, 128)
                    .setBlockData(Bukkit.createBlockData("minecraft:stone_slab[type=bottom]"));
            var destination =
                    new org.bukkit.Location(world, 1600.5, 10.5, 128.5, saved.getYaw(), 0);
            for (Entity part : parts(saved)) part.teleport(destination);
            saved.teleport(destination);
            world.getChunkAt(8, 8).removePluginChunkTicket(this);
            Bukkit.getScheduler()
                    .runTaskLater(
                            this,
                            () -> {
                                try {
                                    check(
                                            world.unloadChunk(100, 8, true),
                                            "decoration chunk unloads");
                                    world.getChunkAt(100, 8).addPluginChunkTicket(this);
                                    world.getChunkAt(100, 8).getEntities();
                                    Bukkit.getScheduler()
                                            .runTaskLater(this, () -> verifyReload(restartId), 10);
                                } catch (Throwable e) {
                                    getLogger()
                                            .log(
                                                    java.util.logging.Level.SEVERE,
                                                    "INTEGRATION CHECK FAILED",
                                                    e);
                                    Bukkit.shutdown();
                                }
                            },
                            20);
            return;
        }
        var match = decor.getClass().getDeclaredMethod("match", ItemStack.class);
        match.setAccessible(true);
        for (String id : decor.getConfig().getConfigurationSection("variants").getKeys(false)) {
            String hatName = decor.getConfig().getString("variants." + id + ".custom-name");
            ItemStack candidate =
                    hatName == null
                            ? item(id)
                            : Bukkit.getItemFactory()
                                    .createItemStack(
                                            "minecraft:carved_pumpkin[minecraft:custom_name={text:\""
                                                    + hatName
                                                    + "\"}]");
            check(match.invoke(decor, candidate) != null, "recognize " + id);
        }
        check(
                match.invoke(decor, new ItemStack(Material.FLINT)) == null,
                "ordinary flint excluded");
        check(
                match.invoke(
                                decor,
                                Bukkit.getItemFactory()
                                        .createItemStack(
                                                "minecraft:flint[minecraft:item_model=\"seedling:seedling\",minecraft:custom_data={seedling_box:1b}]"))
                        == null,
                "unopened seedling excluded");

        Cushion slab =
                place("red_leaf", 128, "minecraft:stone_slab[type=bottom]", .5, EquipmentSlot.HAND);
        check(slab != null, "bottom slab placement");
        check(
                slab.getX() == 128.5 && slab.getZ() == 128.5 && slab.getY() == 10.5,
                "centered X/Z with exact slab height");
        check(
                slab.isInvisible() && parts(slab).size() == 2,
                "hidden cushion with model and punch hitbox");
        check(
                !slab.isVisibleByDefault() && !fake.getBukkitEntity().canSee(slab),
                "backing excluded from client visibility, beyond metadata invisibility");
        check(
                parts(slab).stream().allMatch(fake.getBukkitEntity()::canSee),
                "model and punch hitbox remain visible to player tracking");
        slab.setVisibleByDefault(true); // Simulate a decoration saved by 1.0.0.
        float correctedYaw = slab.getYaw();
        slab.setRotation(correctedYaw + 180, 0);
        slab.getPersistentDataContainer()
                .remove(new NamespacedKey("pikmindecor", "orientation_version"));
        var repairLegacy = decor.getClass().getDeclaredMethod("repair", List.class);
        repairLegacy.setAccessible(true);
        repairLegacy.invoke(decor, List.of(slab.getChunk().getEntities()));
        check(
                !fake.getBukkitEntity().canSee(slab),
                "existing 1.0.0 decoration hidden during repair");
        check(
                Math.abs(slab.getYaw() - correctedYaw) < .01,
                "existing 1.0.0 decoration facing corrected once");
        repairLegacy.invoke(decor, List.of(slab.getChunk().getEntities()));
        check(
                Math.abs(slab.getYaw() - correctedYaw) < .01,
                "repeated repair does not flip facing again");
        ItemDisplay display =
                (ItemDisplay)
                        parts(slab).stream()
                                .filter(ItemDisplay.class::isInstance)
                                .findFirst()
                                .orElseThrow();
        check(
                Math.abs(display.getYaw() - slab.getYaw()) < .01 && display.getPitch() == 0,
                "model fixed upright at placement yaw");
        double yaw = Math.toRadians(display.getYaw());
        // Item-display rendering includes a 180-degree item-model basis rotation.
        Vector forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
        Vector toPlayer =
                fake.getBukkitEntity()
                        .getLocation()
                        .toVector()
                        .subtract(slab.getLocation().toVector())
                        .setY(0)
                        .normalize();
        check(forward.dot(toPlayer) > .999, "model front points toward placing player's position");
        check(
                fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount() == 2,
                "survival consumes exactly one");
        List<Entity> oldParts = parts(slab);
        punch(slab);
        check(
                !slab.isValid() && oldParts.stream().noneMatch(Entity::isValid),
                "one punch removes all decoration entities");
        check(
                world.getEntitiesByClass(Item.class).stream()
                        .anyMatch(e -> e.getItemStack().isSimilar(item("red_leaf"))),
                "drop retains original name, model and custom_data");

        var otherProfile = new GameProfile(UUID.randomUUID(), "NearbyObserver");
        var other =
                new ServerPlayer(
                        server,
                        ((CraftWorld) world).getHandle(),
                        otherProfile,
                        ClientInformation.createDefault());
        other.connection =
                new ServerGamePacketListenerImpl(
                        server,
                        new Connection(PacketFlow.SERVERBOUND),
                        other,
                        CommonListenerCookie.createInitial(otherProfile, false));
        other.setPos(140.6, 11, 128.6);
        ((CraftWorld) world).getHandle().addNewPlayer(other);
        Cushion nearOther = place("purple_leaf", 140, "minecraft:stone", 1, EquipmentSlot.HAND);
        ItemDisplay nearDisplay =
                (ItemDisplay)
                        parts(nearOther).stream()
                                .filter(ItemDisplay.class::isInstance)
                                .findFirst()
                                .orElseThrow();
        double nearYaw = Math.toRadians(nearDisplay.getYaw());
        Vector nearForward = new Vector(-Math.sin(nearYaw), 0, Math.cos(nearYaw));
        Vector toPlacer =
                fake.getBukkitEntity()
                        .getLocation()
                        .toVector()
                        .subtract(nearOther.getLocation().toVector())
                        .setY(0)
                        .normalize();
        check(
                nearForward.dot(toPlacer) > .999,
                "facing uses placer while a second player stands closer");
        float placedYaw = nearDisplay.getYaw();
        other.setPos(138, 11, 128);
        check(
                nearDisplay.getYaw() == placedYaw
                        && nearDisplay.getBillboard() == org.bukkit.entity.Display.Billboard.FIXED,
                "nearby player movement cannot change fixed display facing");
        punch(nearOther);
        other.discard();

        fake.getBukkitEntity().setSneaking(false);
        Block shelfBlock = world.getBlockAt(145, 10, 128);
        shelfBlock.setBlockData(Bukkit.createBlockData("minecraft:oak_shelf[facing=north]"), false);
        ((org.bukkit.block.Shelf) shelfBlock.getState()).getInventory().clear();
        fake.setPos(145.5, 10, 126.5);
        ItemStack shelfItem = item("red_leaf");
        shelfItem.setAmount(3);
        fake.getBukkitEntity().getInventory().setItemInMainHand(shelfItem);
        var shelfHit =
                new net.minecraft.world.phys.BlockHitResult(
                        new net.minecraft.world.phys.Vec3(145.5, 10.5, 128),
                        net.minecraft.core.Direction.NORTH,
                        new net.minecraft.core.BlockPos(145, 10, 128),
                        false);
        fake.gameMode.useItemOn(
                fake,
                ((CraftWorld) world).getHandle(),
                fake.getMainHandItem(),
                net.minecraft.world.InteractionHand.MAIN_HAND,
                shelfHit);
        check(
                java.util.Arrays.stream(
                                ((org.bukkit.block.Shelf) shelfBlock.getState())
                                        .getInventory()
                                        .getContents())
                        .anyMatch(i -> i != null && i.isSimilar(item("red_leaf"))),
                "native shelf click stores Pikmin in a normal item slot");
        check(
                fake.getBukkitEntity().getInventory().getItemInMainHand().isEmpty()
                        && java.util.Arrays.stream(
                                                ((org.bukkit.block.Shelf) shelfBlock.getState())
                                                        .getInventory()
                                                        .getContents())
                                        .filter(i -> i != null && i.isSimilar(item("red_leaf")))
                                        .mapToInt(ItemStack::getAmount)
                                        .sum()
                                == 3,
                "native shelf stores the held stack without losing or duplicating Pikmin");
        fake.gameMode.useItemOn(
                fake,
                ((CraftWorld) world).getHandle(),
                fake.getMainHandItem(),
                net.minecraft.world.InteractionHand.MAIN_HAND,
                shelfHit);
        check(
                fake.getBukkitEntity()
                                .getInventory()
                                .getItemInMainHand()
                                .isSimilar(item("red_leaf"))
                        && fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount()
                                == 3,
                "native shelf click retrieves stored Pikmin stack");
        for (String blockData :
                List.of(
                        "minecraft:chest",
                        "minecraft:oak_trapdoor",
                        "minecraft:stone_button",
                        "minecraft:lever",
                        "minecraft:crafting_table",
                        "minecraft:oak_door",
                        "minecraft:oak_fence_gate")) {
            Block usable = world.getBlockAt(146, 10, 128);
            usable.setBlockData(Bukkit.createBlockData(blockData), false);
            var use =
                    new PlayerInteractEvent(
                            fake.getBukkitEntity(),
                            Action.RIGHT_CLICK_BLOCK,
                            item("red_leaf"),
                            usable,
                            BlockFace.UP,
                            EquipmentSlot.HAND,
                            new Vector(.5, 1, .5));
            Bukkit.getPluginManager().callEvent(use);
            check(
                    use.useInteractedBlock() != org.bukkit.event.Event.Result.DENY,
                    "normal interaction allowed while holding Pikmin: " + blockData);
        }
        Cushion chestDecor = place("blue_leaf", 147, "minecraft:chest", .875, EquipmentSlot.HAND);
        check(chestDecor != null, "sneak-right-click places decoration on chest");
        punch(chestDecor);

        Cushion trapdoor =
                place(
                        "blue_flower",
                        130,
                        "minecraft:oak_trapdoor[half=bottom,open=false]",
                        .1875,
                        EquipmentSlot.OFF_HAND);
        check(
                trapdoor != null && trapdoor.getY() == 10.1875,
                "offhand placement on closed trapdoor at 3/16 height");
        check(
                fake.getBukkitEntity().getInventory().getItemInOffHand().getAmount() == 2,
                "offhand consumes exactly one");

        denyPlacement = true;
        check(
                place("white_bud", 132, "minecraft:stone", 1, EquipmentSlot.HAND) == null,
                "cancelled placement creates no cushion");
        check(
                fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount() == 3,
                "cancelled placement consumes nothing");
        denyPlacement = false;

        fake.getBukkitEntity().setGameMode(GameMode.CREATIVE);
        Cushion creative = place("white_spooky", 133, "minecraft:stone", 1, EquipmentSlot.HAND);
        check(
                creative != null
                        && fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount()
                                == 3,
                "creative placement consumes nothing");
        long itemsBefore = world.getEntitiesByClass(Item.class).size();
        punch(creative);
        check(
                !creative.isValid() && world.getEntitiesByClass(Item.class).size() == itemsBefore,
                "creative breaking follows native no-drop behavior");
        fake.getBukkitEntity().setGameMode(GameMode.SURVIVAL);

        // A future variant is recognized after changing config, without recompilation.
        decor.getConfig().set("variants.green_leaf.material", "FLINT");
        decor.getConfig().set("variants.green_leaf.item-model", "seedling:pikmin/green_leaf");
        decor.getConfig().set("variants.green_leaf.custom-data-key", "seedling_pikmin");
        decor.getConfig().set("variants.green_leaf.custom-data-value", "green_leaf");
        var load = decor.getClass().getDeclaredMethod("loadVariants");
        load.setAccessible(true);
        load.invoke(decor);
        check(
                match.invoke(decor, item("green_leaf")) != null,
                "future variant recognized from config only");
        decor.getConfig().set("variants.green_leaf.scale", -1);
        boolean rejected = false;
        try {
            load.invoke(decor);
        } catch (java.lang.reflect.InvocationTargetException e) {
            rejected = true;
        }
        check(
                rejected && match.invoke(decor, item("green_leaf")) != null,
                "bad reload rejected while previous configuration stays active");
        decor.reloadConfig();
        load.invoke(decor);

        JavaPlugin guard = (JavaPlugin) Bukkit.getPluginManager().getPlugin("SpawnGuard");
        check(guard != null && guard.isEnabled(), "actual SpawnGuard 1.3 loaded");
        guard.getConfig().set("region.world", world.getName());
        guard.getConfig().set("region.x1", 134);
        guard.getConfig().set("region.x2", 138);
        guard.getConfig().set("region.z1", 128);
        guard.getConfig().set("region.z2", 128);
        check(
                place("yellow_leaf", 134, "minecraft:stone", 1, EquipmentSlot.HAND) == null,
                "non-op cannot place inside SpawnGuard region");
        fake.getBukkitEntity().setOp(true);
        Cushion protectedPikmin =
                place("yellow_leaf", 134, "minecraft:stone", 1, EquipmentSlot.HAND);
        check(protectedPikmin != null, "op can place inside SpawnGuard region");
        fake.getBukkitEntity().setOp(false);
        punch(protectedPikmin);
        check(protectedPikmin.isValid(), "non-op cannot punch Pikmin at spawn");
        fake.getBukkitEntity().setOp(true);
        punch(protectedPikmin);
        check(!protectedPikmin.isValid(), "op can punch Pikmin at spawn");
        fake.getBukkitEntity().setOp(false);

        world.getBlockAt(130, 10, 128).setType(Material.AIR);
        Bukkit.getScheduler()
                .runTaskLater(
                        this,
                        () -> {
                            try {
                                check(!trapdoor.isValid(), "native support removal breaks Pikmin");
                                check(
                                        parts(trapdoor).isEmpty(),
                                        "support removal cleans model and hitbox");
                                check(
                                        world.getEntitiesByClass(Item.class).stream()
                                                .anyMatch(
                                                        e ->
                                                                e.getItemStack()
                                                                        .isSimilar(
                                                                                item(
                                                                                        "blue_flower"))),
                                        "support removal drops correct original Pikmin");
                                Cushion persistent =
                                        place(
                                                "purple_spooky",
                                                142,
                                                "minecraft:stone_slab[type=bottom]",
                                                .5,
                                                EquipmentSlot.HAND);
                                check(persistent != null, "restart fixture placed");
                                getConfig()
                                        .set(
                                                "restart-fixture",
                                                persistent.getUniqueId().toString());
                                getConfig().set("restart-yaw", persistent.getYaw());
                                getConfig().set("fixed-facing", true);
                                saveConfig();
                                getLogger()
                                        .info("ALL " + assertions + " INTEGRATION CHECKS PASSED");
                            } catch (Throwable e) {
                                getLogger()
                                        .log(
                                                java.util.logging.Level.SEVERE,
                                                "INTEGRATION CHECK FAILED",
                                                e);
                            } finally {
                                Bukkit.shutdown();
                            }
                        },
                        140);
    }

    private void runHatChecks() throws Exception {
        var sample =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                        new java.io.File(
                                "../../Plugins/PlaceableDecor-source/examples/config-with-hats.yml"));
        var section = sample.getConfigurationSection("variants");
        check(
                section != null && section.getKeys(false).size() == 57,
                "sample contains 20 Pikmin and 37 hats");
        decor.getConfig().set("variants", section);
        var load = decor.getClass().getDeclaredMethod("loadVariants");
        load.setAccessible(true);
        load.invoke(decor);
        var match = decor.getClass().getDeclaredMethod("match", ItemStack.class);
        match.setAccessible(true);
        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".custom-name");
            ItemStack candidate =
                    name == null
                            ? item(id)
                            : Bukkit.getItemFactory()
                                    .createItemStack(
                                            "minecraft:carved_pumpkin[minecraft:custom_name={text:\""
                                                    + name
                                                    + "\"}]");
            check(match.invoke(decor, candidate) != null, "sample recognizes " + id);
        }
        check(
                match.invoke(decor, new ItemStack(Material.CARVED_PUMPKIN)) == null,
                "unnamed pumpkins remain vanilla blocks");
        check(
                match.invoke(
                                decor,
                                Bukkit.getItemFactory()
                                        .createItemStack(
                                                "minecraft:carved_pumpkin[minecraft:custom_name={text:\"Not"
                                                    + " a hat\"}]"))
                        == null,
                "unlisted pumpkin names remain vanilla blocks");
        ItemStack frog =
                Bukkit.getItemFactory()
                        .createItemStack(
                                "minecraft:carved_pumpkin[minecraft:custom_name={text:\"Frog\",color:\"green\"}]");
        check(match.invoke(decor, frog) != null, "formatted custom name matches its plain text");
        frog.setAmount(2);
        fake.getBukkitEntity().getInventory().setItemInMainHand(frog);
        world.getBlockAt(152, 10, 128).setType(Material.STONE);
        fake.setPos(154, 11, 130);
        fake.gameMode.useItemOn(
                fake,
                ((CraftWorld) world).getHandle(),
                fake.getMainHandItem(),
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(
                        new net.minecraft.world.phys.Vec3(152.5, 11, 128.5),
                        net.minecraft.core.Direction.UP,
                        new net.minecraft.core.BlockPos(152, 10, 128),
                        false));
        Cushion hat =
                world
                        .getNearbyEntities(
                                new org.bukkit.Location(world, 152.5, 11, 128.5), .2, .2, .2)
                        .stream()
                        .filter(Cushion.class::isInstance)
                        .map(Cushion.class::cast)
                        .findFirst()
                        .orElse(null);
        check(
                hat != null && !fake.getBukkitEntity().canSee(hat),
                "native named-pumpkin click creates a hidden-backed hat decoration");
        check(
                world.getBlockAt(152, 11, 128).getType().isAir(),
                "hat placement does not place a vanilla pumpkin block");
        check(
                fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount() == 1,
                "hat placement consumes one item");
        check(
                parts(hat).stream()
                        .filter(ItemDisplay.class::isInstance)
                        .map(ItemDisplay.class::cast)
                        .allMatch(d -> d.getItemStack().isSimilar(frog.asOne())),
                "display retains named hat item");
        punch(hat);
        check(
                !hat.isValid()
                        && world.getEntitiesByClass(Item.class).stream()
                                .anyMatch(d -> d.getItemStack().isSimilar(frog.asOne())),
                "hat can be punched and drops the original named item");
        var period =
                Class.forName("org.bukkit.craftbukkit.scheduler.CraftTask")
                        .getDeclaredField("period");
        period.setAccessible(true);
        boolean repeating = false;
        for (var task : Bukkit.getScheduler().getPendingTasks()) {
            if (task.getOwner() == decor && period.getLong(task) > 0) repeating = true;
        }
        check(!repeating, "plugin has no repeating scheduler task");
        benchmarkMatch(match, new ItemStack(Material.STICK), "unrelated stick");
        benchmarkMatch(
                match,
                Bukkit.getItemFactory()
                        .createItemStack(
                                "minecraft:carved_pumpkin[minecraft:custom_name={text:\"Bicorn\"}]"),
                "last configured hat");
        checkFencePlacement(frog);
        // Synthetic players have no view-distance tickets. Load the neighbouring blocks
        // queried by vanilla's pumpkin/golem pattern check at this chunk boundary.
        for (int cx = 8; cx <= 10; cx++) {
            for (int cz = 7; cz <= 9; cz++) world.getChunkAt(cx, cz).addPluginChunkTicket(this);
        }
        world.getBlockAt(159, 10, 128).setType(Material.STONE);
        world.getBlockAt(159, 11, 128).setType(Material.AIR);
        fake.setPos(159.5, 11, 130);
        fake.getBukkitEntity().getInventory().setItemInMainHand(frog.asQuantity(2));
        var fallbackHit =
                new net.minecraft.world.phys.BlockHitResult(
                        new net.minecraft.world.phys.Vec3(159.5, 11, 128.5),
                        net.minecraft.core.Direction.UP,
                        new net.minecraft.core.BlockPos(159, 10, 128),
                        false);
        // Deliberately bypass PlayerInteractEvent as another item-use path could do.
        fake.getMainHandItem()
                .useOn(
                        new net.minecraft.world.item.context.UseOnContext(
                                fake, net.minecraft.world.InteractionHand.MAIN_HAND, fallbackHit));
        check(
                world.getBlockAt(159, 11, 128).getType().isAir(),
                "block-place guard rolls back forced vanilla pumpkin fallback");
        check(
                fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount() == 2,
                "blocked vanilla fallback preserves held hat stack");
        fake.getBukkitEntity()
                .getInventory()
                .setItemInMainHand(new ItemStack(Material.CARVED_PUMPKIN, 2));
        fake.getMainHandItem()
                .useOn(
                        new net.minecraft.world.item.context.UseOnContext(
                                fake, net.minecraft.world.InteractionHand.MAIN_HAND, fallbackHit));
        check(
                world.getBlockAt(159, 11, 128).getType() == Material.CARVED_PUMPKIN,
                "block-place guard still permits ordinary pumpkins");
        world.getBlockAt(159, 11, 128).setType(Material.AIR);
        check(
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pikmindecor status"),
                "on-demand diagnostics command runs");
        check(
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "placeabledecor status"),
                "renamed diagnostics command runs alongside legacy alias");
        getLogger().info("ALL " + assertions + " HAT CONFIG CHECKS PASSED");
        Bukkit.shutdown();
    }

    private void benchmarkMatch(java.lang.reflect.Method match, ItemStack candidate, String label)
            throws Exception {
        for (int i = 0; i < 10000; i++) match.invoke(decor, candidate);
        long[] samples = new long[5];
        for (int sample = 0; sample < samples.length; sample++) {
            long start = System.nanoTime();
            for (int i = 0; i < 10000; i++) match.invoke(decor, candidate);
            samples[sample] = (System.nanoTime() - start) / 10000;
        }
        java.util.Arrays.sort(samples);
        getLogger()
                .info(
                        "BENCHMARK "
                                + decor.getDescription().getVersion()
                                + " "
                                + label
                                + ": "
                                + samples[2]
                                + " ns/match median");
    }

    private void checkFencePlacement(ItemStack original) {
        for (boolean sneak : List.of(false, true)) {
            world.getBlockAt(154, 11, 128).setType(Material.AIR);
            world.getBlockAt(154, 10, 128).setType(Material.OAK_FENCE);
            fake.setPos(156, 11, 130);
            fake.getBukkitEntity().setSneaking(sneak);
            fake.getBukkitEntity().getInventory().setItemInMainHand(original.asQuantity(2));
            fake.gameMode.useItemOn(
                    fake,
                    ((CraftWorld) world).getHandle(),
                    fake.getMainHandItem(),
                    net.minecraft.world.InteractionHand.MAIN_HAND,
                    new net.minecraft.world.phys.BlockHitResult(
                            new net.minecraft.world.phys.Vec3(154.5, 11, 128.5),
                            net.minecraft.core.Direction.UP,
                            new net.minecraft.core.BlockPos(154, 10, 128),
                            false));
            check(
                    world.getBlockAt(154, 11, 128).getType().isAir(),
                    "fence click never places pumpkin; sneaking=" + sneak);
            Cushion hat =
                    world
                            .getNearbyEntities(
                                    new org.bukkit.Location(world, 154.5, 11, 128.5), .2, .2, .2)
                            .stream()
                            .filter(Cushion.class::isInstance)
                            .map(Cushion.class::cast)
                            .findFirst()
                            .orElse(null);
            check(hat != null, "fence top accepts hat decoration; sneaking=" + sneak);
            check(
                    fake.getBukkitEntity().getInventory().getItemInMainHand().getAmount() == 1,
                    "fence hat placement consumes one item; sneaking=" + sneak);
            punch(hat);
        }
    }

    private void verifyReload(String restartId) {
        try {
            Cushion reloaded = (Cushion) Bukkit.getEntity(UUID.fromString(restartId));
            check(
                    reloaded != null && parts(reloaded).size() == 2,
                    "chunk reload retains all three entities without duplicates");
            check(
                    !fake.getBukkitEntity().canSee(reloaded),
                    "backing remains hidden after chunk reload");
            parts(reloaded).stream()
                    .filter(ItemDisplay.class::isInstance)
                    .findFirst()
                    .orElseThrow()
                    .remove();
            var repair = decor.getClass().getDeclaredMethod("repair", List.class);
            repair.setAccessible(true);
            repair.invoke(decor, List.of(reloaded.getChunk().getEntities()));
            check(parts(reloaded).size() == 2, "missing model repairs without duplicating hitbox");
            punch(reloaded);
            check(
                    !reloaded.isValid() && parts(reloaded).isEmpty(),
                    "restored decoration remains punchable and cleans up");
            getLogger().info("ALL " + assertions + " RESTART/CHUNK CHECKS PASSED");
        } catch (Throwable e) {
            getLogger().log(java.util.logging.Level.SEVERE, "INTEGRATION CHECK FAILED", e);
        } finally {
            getConfig().set("restart-fixture", null);
            saveConfig();
            Bukkit.shutdown();
        }
    }
}
