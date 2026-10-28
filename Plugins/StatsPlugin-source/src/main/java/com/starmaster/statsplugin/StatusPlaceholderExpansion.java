package com.starmaster.statsplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Exposes player status tags to PlaceholderAPI. */
public class StatusPlaceholderExpansion extends PlaceholderExpansion {

    private final StatsPlugin plugin;

    public StatusPlaceholderExpansion(StatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "statsplugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return "StarMaster";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("afk")) {
            return plugin.isAfk(player) ? "§7[AFK] " : "";
        }
        if (params.equalsIgnoreCase("afk_tag")) {
            return plugin.isAfk(player) ? "§7[AFK]" : "";
        }

        if (params.equalsIgnoreCase("rec")) {
            return plugin.isRecording(player) ? "§c[REC] " : "";
        }
        if (params.equalsIgnoreCase("rec_tag")) {
            return plugin.isRecording(player) ? "§c[REC]" : "";
        }

        if (params.equalsIgnoreCase("live")) {
            return plugin.isLive(player) ? "§d[LIVE] " : "";
        }
        if (params.equalsIgnoreCase("live_tag")) {
            return plugin.isLive(player) ? "§d[LIVE]" : "";
        }

        return null; // not a placeholder this expansion handles
    }
}
