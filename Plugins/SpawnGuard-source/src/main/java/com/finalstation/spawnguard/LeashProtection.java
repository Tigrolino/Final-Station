package com.finalstation.spawnguard;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class LeashProtection implements Listener {
    private final Map<UUID, Integer> brokenSupports = new HashMap<>();

    private boolean playerCaused(Entity entity) {
        return entity instanceof Player
                || entity instanceof Projectile projectile
                        && projectile.getShooter() instanceof Player;
    }

    private boolean fenceHelper(Entity entity) {
        return entity.getScoreboardTags().contains("leash_fences.leash_entity")
                || entity.getScoreboardTags().contains("leash_fences.dummy_leash_entity");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof LeashHitch knot)) {
            return;
        }
        if (event instanceof HangingBreakByEntityEvent byEntity
                && playerCaused(byEntity.getRemover())) {
            brokenSupports.remove(knot.getUniqueId());
            return;
        }
        Integer expiry = brokenSupports.get(knot.getUniqueId());
        if (event.getCause() == HangingBreakEvent.RemoveCause.PHYSICS
                && expiry != null
                && expiry >= Bukkit.getCurrentTick()) {
            brokenSupports.remove(knot.getUniqueId());
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFenceBreak(BlockBreakEvent event) {
        if (!Tag.FENCES.isTagged(event.getBlock().getType())) {
            return;
        }
        int tick = Bukkit.getCurrentTick();
        brokenSupports.values().removeIf(expiry -> expiry < tick);
        for (Entity entity :
                event.getBlock()
                        .getWorld()
                        .getNearbyEntities(
                                event.getBlock().getLocation().add(0.5, 0.5, 0.5), 0.6, 0.6, 0.6)) {
            if (entity instanceof LeashHitch
                    && entity.getLocation().getBlock().equals(event.getBlock())) {
                // Hanging entities check their support periodically, after BlockBreakEvent returns.
                brokenSupports.put(entity.getUniqueId(), tick + 200);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LeashHitch) && !fenceHelper(entity)) {
            return;
        }
        // The datapack removes its helper entities with /kill during normal lead removal.
        if (event.getCause() == EntityDamageEvent.DamageCause.KILL && fenceHelper(entity)) {
            return;
        }
        if (!(event instanceof EntityDamageByEntityEvent byEntity)
                || !playerCaused(byEntity.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnleash(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            event.setCancelled(true);
        }
    }
}
