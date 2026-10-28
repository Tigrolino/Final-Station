package com.finalstation.spawnguard;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.finalstation.spawnguard.SpawnGuard.Settings;

import io.papermc.paper.event.block.PlayerShearBlockEvent;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * All SpawnGuard rules.
 *
 * <p>Players: operators may do anything (if ops-bypass is on). Everyone else is stopped silently,
 * with a puff of smoke on the block instead of a chat message.
 *
 * <p>Environment: water, lava, pistons, explosions, fire and mobs are stopped for everyone, because
 * there is no player to ask.
 */
final class GuardListener implements Listener {

    private final SpawnGuard plugin;

    GuardListener(SpawnGuard plugin) {
        this.plugin = plugin;
    }

    // HELPERS

    private Settings cfg() {
        return plugin.settings();
    }

    private boolean inRegion(World world, int x, int z) {
        Settings s = cfg();
        return s.regionEnabled()
                && x >= s.minX()
                && x <= s.maxX()
                && z >= s.minZ()
                && z <= s.maxZ()
                && world.getName().equals(s.world());
    }

    private boolean inRegion(Block block) {
        return inRegion(block.getWorld(), block.getX(), block.getZ());
    }

    private boolean inRegion(Location location) {
        return location.getWorld() != null
                && inRegion(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    private boolean mayBuild(Player player) {
        return cfg().opsBypass() && player.isOp();
    }

    /** The player behind an entity: the player itself, or whoever shot the projectile. */
    private Player playerBehind(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (entity instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    /** Same effect WorldGuard shows on a denied action. Only the player sees it. */
    private void smoke(Player player, Location location) {
        if (cfg().smokeEffect()) {
            player.playEffect(location, Effect.SMOKE, BlockFace.UP);
        }
    }

    private void deny(Cancellable event, Player player, Block block) {
        event.setCancelled(true);
        smoke(player, block.getLocation());
    }

    /**
     * Mobs from no-block-damage-mobs (endermen, creepers): never allowed to change blocks, in any
     * world.
     */
    private boolean harmless(Entity entity) {
        return entity != null && cfg().noBlockDamage().contains(entity.getType());
    }

    // VINE GROWTH (all worlds)

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Material newType = event.getNewState().getType();
        if (cfg().blockedGrowth().contains(newType)) {
            event.setCancelled(true);
            return;
        }

        // Region: no fire spread, and nothing spreads in from outside (grass, sculk, mushrooms...).
        if (inRegion(event.getBlock())) {
            if (newType == Material.FIRE
                    || newType == Material.SOUL_FIRE
                    || !inRegion(event.getSource())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        if (cfg().blockedGrowth().contains(event.getNewState().getType())) {
            event.setCancelled(true);
        }
    }

    /**
     * Vines no longer grow on their own, so players can grow them by hand: right-click a vine with
     * bone meal and it grows one block. See {@link VineGrowth} for where.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBonemealVine(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        ItemStack item = event.getItem();
        if (!cfg().bonemealVines()
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() == null
                || clicked == null
                || clicked.getType() != Material.VINE
                || item == null
                || item.getType() != Material.BONE_MEAL
                || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        VineGrowth.Step step = VineGrowth.next(clicked);
        if (step == null) {
            return; // nowhere to grow: nothing happens and no bone meal is used
        }
        Block target = step.target();

        Player player = event.getPlayer();
        event.setCancelled(true);

        // Place it like a normal block placement, so the spawn region (and any other
        // protection plugin) gets to say no.
        BlockState before = target.getState();
        target.setBlockData(step.data(), true);
        BlockPlaceEvent place =
                new BlockPlaceEvent(
                        target, before, step.from(), item, player, true, event.getHand());
        plugin.getServer().getPluginManager().callEvent(place);
        if (place.isCancelled() || !place.canBuild()) {
            before.update(true, false);
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = player.getInventory().getItem(event.getHand());
            inHand.setAmount(inHand.getAmount() - 1);
        }
        player.swingHand(event.getHand());
        // The green sparkles and the sound of normal bone meal. (The game's built-in bone meal
        // effect draws nothing on vines, so both are sent directly.)
        Location center = target.getLocation().add(0.5, 0.5, 0.5);
        target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center, 15, 0.3, 0.3, 0.3, 0);
        target.getWorld()
                .playSound(center, Sound.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    // REGION: PLAYERS BREAKING AND PLACING

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (inRegion(event.getBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (mayBuild(event.getPlayer())) {
            return;
        }
        boolean touches = inRegion(event.getBlock());
        if (!touches && event instanceof BlockMultiPlaceEvent multi) {
            // Beds, doors, tall flowers: either half inside the region counts.
            for (BlockState state : multi.getReplacedBlockStates()) {
                if (inRegion(state.getLocation())) {
                    touches = true;
                    break;
                }
            }
        }
        if (touches) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (inRegion(event.getBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (inRegion(event.getBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || !inRegion(block) || mayBuild(event.getPlayer())) {
            return;
        }

        // Jumping on farmland or eggs.
        if (event.getAction() == Action.PHYSICAL) {
            Material type = block.getType();
            if (type == Material.FARMLAND
                    || type == Material.TURTLE_EGG
                    || type == Material.SNIFFER_EGG) {
                event.setCancelled(true);
            }
            return;
        }

        // Tools that change a block on right click: stripping logs, making paths, tilling,
        // waxing or scraping copper, brushing. Doors, buttons and chests stay usable.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Signs: no editing, dyeing or waxing.
            if (Tag.ALL_SIGNS.isTagged(block.getType())) {
                event.setUseInteractedBlock(Event.Result.DENY);
            }
            ItemStack item = event.getItem();
            if (item == null) {
                return;
            }
            Material type = item.getType();
            if (Tag.ITEMS_AXES.isTagged(type)
                    || Tag.ITEMS_SHOVELS.isTagged(type)
                    || Tag.ITEMS_HOES.isTagged(type)
                    || type == Material.HONEYCOMB
                    || type == Material.BRUSH) {
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        if (player == null || mayBuild(player)) {
            return;
        }
        boolean touches = inRegion(event.getBlock());
        for (BlockState state : event.getBlocks()) {
            touches = touches || inRegion(state.getLocation());
        }
        if (touches) {
            deny(event, player, event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        if (inRegion(event.getHarvestedBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getHarvestedBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onShearBlock(PlayerShearBlockEvent event) {
        if (inRegion(event.getBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFlowerPot(PlayerFlowerPotManipulateEvent event) {
        if (inRegion(event.getFlowerpot()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getFlowerpot());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onOpenSign(PlayerOpenSignEvent event) {
        Block block = event.getSign().getBlock();
        if (inRegion(block) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), block);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (inRegion(event.getBlock()) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTakeLecternBook(PlayerTakeLecternBookEvent event) {
        Block block = event.getLectern().getBlock();
        if (inRegion(block) && !mayBuild(event.getPlayer())) {
            deny(event, event.getPlayer(), block);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCauldronChange(CauldronLevelChangeEvent event) {
        // Filling or emptying a cauldron with a bucket or bottle, washing armor in it.
        if (event.getReason() != CauldronLevelChangeEvent.ChangeReason.EXTINGUISH
                && event.getEntity() instanceof Player player
                && inRegion(event.getBlock())
                && !mayBuild(player)) {
            deny(event, player, event.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTntPrime(TNTPrimeEvent event) {
        if (!inRegion(event.getBlock())) {
            return;
        }
        Player player =
                event.getPrimingEntity() == null ? null : playerBehind(event.getPrimingEntity());
        if (player != null && mayBuild(player)) {
            return;
        }
        event.setCancelled(true);
        if (player != null) {
            smoke(player, event.getBlock().getLocation());
        }
    }

    // REGION: ITEM FRAMES, PAINTINGS, ARMOR STANDS

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Hanging hanging = event.getEntity();
        // A creeper exploding next to a painting or item frame, anywhere.
        if (event instanceof HangingBreakByEntityEvent byMob && harmless(byMob.getRemover())) {
            event.setCancelled(true);
            return;
        }
        if (!inRegion(hanging.getLocation())) {
            return;
        }
        if (event instanceof HangingBreakByEntityEvent byEntity) {
            Player player = playerBehind(byEntity.getRemover());
            if (player != null && mayBuild(player)) {
                return;
            }
            event.setCancelled(true);
            if (player != null) {
                smoke(player, hanging.getLocation());
            }
        } else if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null && inRegion(event.getEntity().getLocation()) && !mayBuild(player)) {
            event.setCancelled(true);
            smoke(player, event.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemFrameChange(PlayerItemFrameChangeEvent event) {
        // Rotating and filling an empty frame stay allowed; taking the item out does not.
        if (event.getAction() == PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE
                && inRegion(event.getItemFrame().getLocation())
                && !mayBuild(event.getPlayer())) {
            event.setCancelled(true);
            smoke(event.getPlayer(), event.getItemFrame().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (inRegion(event.getRightClicked().getLocation()) && !mayBuild(event.getPlayer())) {
            event.setCancelled(true);
            smoke(event.getPlayer(), event.getRightClicked().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        // Armor stands and end crystals. Boats and minecarts stay allowed.
        Player player = event.getPlayer();
        if (player == null || event.getEntity() instanceof Vehicle) {
            return;
        }
        if (inRegion(event.getEntity().getLocation()) && !mayBuild(player)) {
            deny(event, player, event.getBlock());
        }
    }

    // REGION: DAMAGE (decoration entities and PvP)

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        Player attacker =
                event instanceof EntityDamageByEntityEvent byEntity
                        ? playerBehind(byEntity.getDamager())
                        : null;

        if (victim instanceof ArmorStand || victim instanceof Hanging) {
            // A creeper explosion still hurts players and mobs, but not decorations.
            if (event instanceof EntityDamageByEntityEvent byMob && harmless(byMob.getDamager())) {
                event.setCancelled(true);
                return;
            }
            if (!inRegion(victim.getLocation())) {
                return;
            }
            if (attacker != null) {
                if (!mayBuild(attacker)) {
                    event.setCancelled(true);
                    smoke(attacker, victim.getLocation());
                }
            } else {
                // Explosions, arrows from dispensers, mobs.
                event.setCancelled(true);
            }
            return;
        }

        if (cfg().noPvp()
                && victim instanceof Player target
                && attacker != null
                && attacker != target
                && (inRegion(target.getLocation()) || inRegion(attacker.getLocation()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!cfg().noPvp() || !(event.getPotion().getShooter() instanceof Player thrower)) {
            return;
        }
        boolean potionInRegion = inRegion(event.getPotion().getLocation());
        for (LivingEntity affected : event.getAffectedEntities()) {
            if (affected instanceof Player target
                    && target != thrower
                    && (potionInRegion || inRegion(target.getLocation()))) {
                event.setIntensity(target, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLingeringPotion(AreaEffectCloudApplyEvent event) {
        if (!cfg().noPvp() || !(event.getEntity().getSource() instanceof Player thrower)) {
            return;
        }
        boolean cloudInRegion = inRegion(event.getEntity().getLocation());
        event.getAffectedEntities()
                .removeIf(
                        affected ->
                                affected instanceof Player target
                                        && target != thrower
                                        && (cloudInRegion || inRegion(target.getLocation())));
    }

    // REGION: MOBS AND OTHER ENTITIES CHANGING BLOCKS

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // Endermen picking up or putting down blocks, anywhere.
        if (harmless(event.getEntity())) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if (!inRegion(block)) {
            return;
        }

        // Only the state changes, the block stays the same block: redstone ore lighting up,
        // an arrow lighting a campfire, bees helping crops. Harmless, leave it alone.
        if (event.getTo() == block.getType()) {
            return;
        }

        Entity entity = event.getEntity();
        if (entity instanceof FallingBlock falling) {
            // Sand and gravel that started inside the region may fall and land normally.
            // Anything that came flying in from outside may not land.
            Location origin = falling.getOrigin();
            if (event.getTo().isAir() || origin == null || inRegion(origin)) {
                return;
            }
            event.setCancelled(true);
            return;
        }

        Player player = playerBehind(entity);
        if (player != null) {
            if (!mayBuild(player)) {
                deny(event, player, block);
            }
            return;
        }

        // Endermen, silverfish, ravagers, withers, zombies breaking doors, sheep eating grass,
        // mobs trampling farmland, boats breaking lily pads...
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        // Snow golem trails and frost walker ice.
        if (!inRegion(event.getBlock())) {
            return;
        }
        if (event.getEntity() instanceof Player player && mayBuild(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        if (inRegion(block)) {
            Material type = block.getType();
            if (type == Material.FARMLAND
                    || type == Material.TURTLE_EGG
                    || type == Material.SNIFFER_EGG) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        SpawnReason reason = event.getSpawnReason();
        if (reason == SpawnReason.COMMAND
                || reason == SpawnReason.CUSTOM
                || reason == SpawnReason.SPAWNER_EGG) {
            return;
        }
        if (cfg().blockedSpawns().contains(event.getEntityType())
                && inRegion(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAnvilDamaged(AnvilDamagedEvent event) {
        // Anvils normally chip a little with every use until they break. Not in the region.
        Location location = event.getInventory().getLocation();
        if (cfg().unbreakableAnvils() && location != null && inRegion(location)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (cfg().noHungerDrain()
                && event.getEntity() instanceof Player player
                && event.getFoodLevel() < player.getFoodLevel()
                && inRegion(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    // REGION: WATER, LAVA, PISTONS, EXPLOSIONS, FIRE

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        Block to = event.getToBlock();
        if (!inRegion(to)) {
            return;
        }
        // Nothing flows in from outside, and inside the region a flow may only move into
        // empty space - it can never wash away a torch, a flower, redstone and so on.
        // (A dragon egg teleporting counts as a "flow" too and is stopped by the same check.)
        boolean fromOutside = !inRegion(event.getBlock());
        boolean wouldBreakSomething = !to.getType().isAir() && !to.isLiquid();
        if (fromOutside
                || wouldBreakSomething
                || event.getBlock().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    private boolean pistonTouchesRegion(Block piston, List<Block> moved, BlockFace direction) {
        if (inRegion(piston) || inRegion(piston.getRelative(direction))) {
            return true;
        }
        for (Block block : moved) {
            if (inRegion(block) || inRegion(block.getRelative(direction))) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (cfg().blockPistons()
                && pistonTouchesRegion(event.getBlock(), event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (cfg().blockPistons()
                && pistonTouchesRegion(event.getBlock(), event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Creepers, anywhere: the explosion still happens and still hurts, it just breaks no
        // blocks.
        if (harmless(event.getEntity())) {
            event.blockList().clear();
            return;
        }
        // Everything else: the explosion cannot touch blocks inside the region.
        event.blockList().removeIf(this::inRegion);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::inRegion);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (inRegion(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!inRegion(event.getBlock())) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null && event.getIgnitingEntity() != null) {
            player = playerBehind(event.getIgnitingEntity());
        }
        if (player != null && mayBuild(player)) {
            return;
        }
        // Lava, lightning, fire spread, fireballs - and flint and steel in the wrong hands.
        event.setCancelled(true);
        if (player != null) {
            smoke(player, event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        // A dispenser standing outside the region must not shoot water, lava or fire into it.
        Block dispenser = event.getBlock();
        if (!inRegion(dispenser)
                && dispenser.getBlockData() instanceof Directional directional
                && inRegion(dispenser.getRelative(directional.getFacing()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        Player player = event.getPlayer();
        if (inRegion(event.getLocation())) {
            // Trees inside the region grow on their own, but not from a visitor's bone meal.
            if (player != null && !mayBuild(player)) {
                event.setCancelled(true);
                smoke(player, event.getLocation());
            }
            return;
        }
        // A tree growing right outside the border must not put leaves or logs inside.
        event.getBlocks().removeIf(state -> inRegion(state.getLocation()));
    }
}
