package com.finalstation.spawnguard;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Small jobs that WorldGuard used to do on The Final Station: stop vines from growing, stop
 * endermen and creepers from changing blocks, protect leads and protect the spawn region.
 */
public final class SpawnGuard extends JavaPlugin {

    private Settings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(new GuardListener(this), this);
        getServer().getPluginManager().registerEvents(new LeashProtection(), this);
    }

    Settings settings() {
        return settings;
    }

    private void loadSettings() {
        FileConfiguration cfg = getConfig();

        Set<Material> growth = EnumSet.noneOf(Material.class);
        for (String name : cfg.getStringList("blocked-growth")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                getLogger().warning("blocked-growth: unknown block '" + name + "', skipping it.");
            } else {
                growth.add(material);
            }
        }

        Set<EntityType> spawns = readMobs(cfg, "region.blocked-mob-spawns");
        Set<EntityType> noBlockDamage = readMobs(cfg, "no-block-damage-mobs");

        int x1 = cfg.getInt("region.x1");
        int x2 = cfg.getInt("region.x2");
        int z1 = cfg.getInt("region.z1");
        int z2 = cfg.getInt("region.z2");

        settings =
                new Settings(
                        growth,
                        cfg.getBoolean("bonemeal-vines", true),
                        cfg.getBoolean("region.enabled", true),
                        cfg.getString("region.world", "world"),
                        Math.min(x1, x2),
                        Math.max(x1, x2),
                        Math.min(z1, z2),
                        Math.max(z1, z2),
                        cfg.getBoolean("region.ops-bypass", true),
                        cfg.getBoolean("region.smoke-effect", true),
                        cfg.getBoolean("region.block-pistons", true),
                        cfg.getBoolean("region.no-pvp", true),
                        cfg.getBoolean("region.no-hunger-drain", true),
                        cfg.getBoolean("region.unbreakable-anvils", true),
                        spawns,
                        noBlockDamage);

        getLogger().info("Blocking growth of " + growth + " in all worlds.");
        getLogger().info("Mobs that cannot change blocks in any world: " + noBlockDamage + ".");
        if (settings.regionEnabled()) {
            getLogger()
                    .info(
                            "Protecting x "
                                    + settings.minX()
                                    + ".."
                                    + settings.maxX()
                                    + ", z "
                                    + settings.minZ()
                                    + ".."
                                    + settings.maxZ()
                                    + " in world '"
                                    + settings.world()
                                    + "'.");
        } else {
            getLogger().info("Region protection is disabled in config.yml.");
        }
    }

    private Set<EntityType> readMobs(FileConfiguration cfg, String path) {
        Set<EntityType> mobs = EnumSet.noneOf(EntityType.class);
        for (String name : cfg.getStringList(path)) {
            String cleaned = name.trim().toUpperCase(Locale.ROOT).replace("MINECRAFT:", "");
            try {
                mobs.add(EntityType.valueOf(cleaned));
            } catch (IllegalArgumentException e) {
                getLogger().warning(path + ": unknown mob '" + name + "', skipping it.");
            }
        }
        return mobs;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            loadSettings();
            sender.sendMessage("\u00a7aSpawnGuard config reloaded.");
            return true;
        }
        return false;
    }

    /** Everything read from config.yml. Replaced as a whole on /spawnguard reload. */
    record Settings(
            Set<Material> blockedGrowth,
            boolean bonemealVines,
            boolean regionEnabled,
            String world,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            boolean opsBypass,
            boolean smokeEffect,
            boolean blockPistons,
            boolean noPvp,
            boolean noHungerDrain,
            boolean unbreakableAnvils,
            Set<EntityType> blockedSpawns,
            Set<EntityType> noBlockDamage) {}
}
