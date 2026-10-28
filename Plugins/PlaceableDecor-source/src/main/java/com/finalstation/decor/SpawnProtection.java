package com.finalstation.decor;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** Read SpawnGuard's live config, including changes made with /spawnguard reload. */
final class SpawnProtection {
    private final JavaPlugin owner;

    SpawnProtection(JavaPlugin owner) {
        this.owner = owner;
    }

    boolean denied(Player player, Location at) {
        var plugin = owner.getServer().getPluginManager().getPlugin("SpawnGuard");
        if (!(plugin instanceof JavaPlugin guard) || !guard.isEnabled()) return false;
        FileConfiguration c = guard.getConfig();
        if (!c.getBoolean("region.enabled", true)
                || at.getWorld() == null
                || !at.getWorld().getName().equals(c.getString("region.world", "world")))
            return false;
        boolean inside =
                inside(at.getBlockX(), c.getInt("region.x1"), c.getInt("region.x2"))
                        && inside(at.getBlockZ(), c.getInt("region.z1"), c.getInt("region.z2"));
        return inside
                && (player == null || !player.isOp() || !c.getBoolean("region.ops-bypass", true));
    }

    static boolean inside(int value, int a, int b) {
        return value >= Math.min(a, b) && value <= Math.max(a, b);
    }
}
