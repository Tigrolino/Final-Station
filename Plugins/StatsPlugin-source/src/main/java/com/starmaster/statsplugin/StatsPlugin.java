package com.starmaster.statsplugin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsPlugin extends JavaPlugin implements Listener {

    private Map<UUID, Integer> currentPage;
    private Map<String, List<PlayerStat>> cachedStats;
    private final Map<UUID, ItemStack> headTextureCache;
    private long lastCacheUpdate;
    private long cacheDurationMs;
    private long lastSkinCacheClear;
    private long skinCacheDurationMs;
    private int totalAdvancementCount;

    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Long> afkSince = new HashMap<>();
    private Map<UUID, Long> totalAfkSeconds = new HashMap<>();
    private boolean afkEnabled;
    private long afkThresholdMs;
    private String nowAfkMessage;
    private String noLongerAfkMessage;
    private File afkDataFile;

    private final Set<UUID> recordingPlayers = new HashSet<>();
    private final Set<UUID> livePlayers = new HashSet<>();
    private boolean recordingEnabled;
    private String nowRecordingMessage;
    private String noLongerRecordingMessage;
    private boolean liveEnabled;
    private String nowLiveMessage;
    private String noLongerLiveMessage;

    private int itemsPerPage;
    private String mainMenuTitle;
    private int mainMenuSize;
    private Map<String, MenuItemConfig> menuItems;

    private static final Map<String, String> CONFIG_KEY_TO_TYPE = new LinkedHashMap<>();

    static {
        CONFIG_KEY_TO_TYPE.put("kills", "Kills");
        CONFIG_KEY_TO_TYPE.put("deaths", "Deaths");
        CONFIG_KEY_TO_TYPE.put("playtime", "Playtime");
        CONFIG_KEY_TO_TYPE.put("advancements", "Advancements");
        CONFIG_KEY_TO_TYPE.put("distance", "Distance Traveled");
        CONFIG_KEY_TO_TYPE.put("afk-time", "AFK Time");
    }

    public StatsPlugin() {
        this.currentPage = new HashMap<>();
        this.cachedStats = new ConcurrentHashMap<>();
        this.headTextureCache = new ConcurrentHashMap<>();
        this.lastCacheUpdate = 0L;
        this.lastSkinCacheClear = System.currentTimeMillis();
    }

    @Override
    public void onEnable() {
        getLogger().info("StatsPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        saveDefaultConfig();
        loadConfig();

        new BukkitRunnable() {
            @Override
            public void run() {
                updateStatsCache();
            }
        }.runTaskAsynchronously(this);

        getDataFolder().mkdirs();
        loadAfkData();

        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            lastActivity.put(p.getUniqueId(), now);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkAfkTimeouts();
            }
        }.runTaskTimer(this, 20L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                saveAfkData();
            }
        }.runTaskTimer(this, 6000L, 6000L);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StatusPlaceholderExpansion(this).register();
            getLogger()
                    .info(
                            "Hooked into PlaceholderAPI - add %statsplugin_afk%, %statsplugin_rec%,"
                                + " and/or %statsplugin_live% to TAB's nametag/tablist format to"
                                + " show those tags.");
        } else {
            getLogger()
                    .info(
                            "PlaceholderAPI not found - AFK/recording/live chat messages still"
                                + " work, but the tab/nametag tags need PlaceholderAPI installed to"
                                + " display.");
        }
    }

    @Override
    public void onDisable() {
        headTextureCache.clear();

        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, Long> entry : new HashMap<>(afkSince).entrySet()) {
            long seconds = (now - entry.getValue()) / 1000;
            totalAfkSeconds.merge(entry.getKey(), seconds, Long::sum);
        }
        afkSince.clear();
        saveAfkData();

        getLogger().info("StatsPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stats")
                && args.length > 0
                && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("statsplugin.reload")) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }
            loadConfig();
            sender.sendMessage("§aStatsPlugin config reloaded.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("stats")) {
            openMainMenu(player);
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            return true;
        }

        if (command.getName().equalsIgnoreCase("afk")) {
            if (!afkEnabled) {
                player.sendMessage("§cAFK tracking is disabled on this server.");
                return true;
            }
            boolean currentlyAfk = afkSince.containsKey(player.getUniqueId());
            setAfk(player, !currentlyAfk);
            return true;
        }

        if (command.getName().equalsIgnoreCase("recording")) {
            if (!recordingEnabled) {
                player.sendMessage("§cThe recording indicator is disabled on this server.");
                return true;
            }
            UUID uuid = player.getUniqueId();
            if (recordingPlayers.remove(uuid)) {
                player.sendMessage(noLongerRecordingMessage);
            } else {
                recordingPlayers.add(uuid);
                player.sendMessage(nowRecordingMessage);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("live")) {
            if (!liveEnabled) {
                player.sendMessage("§cThe live indicator is disabled on this server.");
                return true;
            }
            UUID uuid = player.getUniqueId();
            if (livePlayers.remove(uuid)) {
                player.sendMessage(noLongerLiveMessage);
            } else {
                livePlayers.add(uuid);
                player.sendMessage(nowLiveMessage);
            }
            return true;
        }

        return false;
    }

    /**
     * The single source of truth for "is this player afk right now" - presence in afkSince. Used by
     * the /afk command, the AFK-timeout checker, and the PlaceholderAPI expansion.
     */
    public boolean isAfk(Player player) {
        return afkSince.containsKey(player.getUniqueId());
    }

    /** Used by /recording and the PlaceholderAPI expansion. */
    public boolean isRecording(Player player) {
        return recordingPlayers.contains(player.getUniqueId());
    }

    /** Used by /live and the PlaceholderAPI expansion. */
    public boolean isLive(Player player) {
        return livePlayers.contains(player.getUniqueId());
    }

    /**
     * Reads config.yml (via Bukkit's normal reloadConfig()/getConfig()) into this plugin's
     * in-memory settings. Called once from onEnable(), and again any time /stats reload runs -
     * every setting it touches is read fresh here rather than cached anywhere else, so a reload
     * always takes effect immediately.
     */
    private void loadConfig() {
        reloadConfig();
        FileConfiguration config = getConfig();

        afkEnabled = config.getBoolean("afk.enabled", true);
        afkThresholdMs = Math.max(1, config.getLong("afk.timeout-seconds", 60)) * 1000L;
        nowAfkMessage =
                ChatColor.translateAlternateColorCodes(
                        '&', config.getString("afk.now-afk-message", "&7You're now AFK"));
        noLongerAfkMessage =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString("afk.no-longer-afk-message", "&7You're no longer AFK"));

        recordingEnabled = config.getBoolean("recording.enabled", true);
        nowRecordingMessage =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString(
                                "recording.now-recording-message",
                                "&cRecording indicator turned on"));
        noLongerRecordingMessage =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString(
                                "recording.no-longer-recording-message",
                                "&7Recording indicator turned off"));

        liveEnabled = config.getBoolean("live.enabled", true);
        nowLiveMessage =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString("live.now-live-message", "&dLive indicator turned on"));
        noLongerLiveMessage =
                ChatColor.translateAlternateColorCodes(
                        '&',
                        config.getString(
                                "live.no-longer-live-message", "&7Live indicator turned off"));

        cacheDurationMs = Math.max(1, config.getLong("cache.stats-refresh-seconds", 30)) * 1000L;
        skinCacheDurationMs =
                Math.max(1, config.getLong("cache.head-cache-clear-hours", 1)) * 3_600_000L;

        int configuredItemsPerPage = config.getInt("leaderboard.items-per-page", 45);
        if (configuredItemsPerPage < 1 || configuredItemsPerPage > 45) {
            getLogger()
                    .warning(
                            "leaderboard.items-per-page ("
                                    + configuredItemsPerPage
                                    + ") must be between 1 and 45 (the leaderboard screen's bottom"
                                    + " row is reserved for page controls) - clamping.");
        }
        itemsPerPage = Math.min(Math.max(configuredItemsPerPage, 1), 45);

        mainMenuTitle =
                ChatColor.translateAlternateColorCodes(
                        '&', config.getString("main-menu.title", "&6&lServer Statistics"));

        int configuredSize = config.getInt("main-menu.size", 27);
        if (configuredSize < 9 || configuredSize > 54 || configuredSize % 9 != 0) {
            getLogger()
                    .warning(
                            "main-menu.size ("
                                    + configuredSize
                                    + ") must be a multiple of 9 between 9 and 54 - using 27"
                                    + " instead.");
            configuredSize = 27;
        }
        mainMenuSize = configuredSize;

        Map<String, MenuItemConfig> newMenuItems = new LinkedHashMap<>();
        Map<Integer, String> seenSlots = new HashMap<>();
        for (Map.Entry<String, String> entry : CONFIG_KEY_TO_TYPE.entrySet()) {
            String configKey = entry.getKey();
            String type = entry.getValue();
            String path = "main-menu.items." + configKey;

            MenuItemConfig item = new MenuItemConfig();
            item.enabled = config.getBoolean(path + ".enabled", true);
            item.slot = config.getInt(path + ".slot", -1);

            String materialName = config.getString(path + ".material", "STONE");
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                getLogger()
                        .warning(
                                "Invalid material '"
                                        + materialName
                                        + "' for '"
                                        + configKey
                                        + "' in config.yml - using STONE instead.");
                material = Material.STONE;
            }
            item.material = material;

            item.displayName =
                    ChatColor.translateAlternateColorCodes(
                            '&', config.getString(path + ".name", type));

            List<String> lore = new ArrayList<>();
            for (String line : config.getStringList(path + ".lore")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            item.lore = lore;

            if (item.enabled && (item.slot < 0 || item.slot >= mainMenuSize)) {
                getLogger()
                        .warning(
                                "Slot "
                                        + item.slot
                                        + " for '"
                                        + configKey
                                        + "' in config.yml is outside the main menu (0-"
                                        + (mainMenuSize - 1)
                                        + ") - disabling this item until it's fixed.");
                item.enabled = false;
            }

            if (item.enabled) {
                String existing = seenSlots.put(item.slot, configKey);
                if (existing != null) {
                    getLogger()
                            .warning(
                                    "Both '"
                                            + existing
                                            + "' and '"
                                            + configKey
                                            + "' are set to slot "
                                            + item.slot
                                            + " in config.yml - '"
                                            + configKey
                                            + "' will be shown on top of it.");
                }
            }

            newMenuItems.put(type, item);
        }
        menuItems = newMenuItems;
    }

    /**
     * Reverse lookup for onInventoryClick: given a slot the player clicked in the main menu, which
     * stat's leaderboard should open? Slot-based rather than matching on display name/text, since
     * names are now configurable and text-matching would break the moment someone renamed an item.
     */
    private String getTypeForSlot(int slot) {
        for (Map.Entry<String, MenuItemConfig> entry : menuItems.entrySet()) {
            MenuItemConfig item = entry.getValue();
            if (item.enabled && item.slot == slot) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Called by every "real activity" event. Refreshes the idle timer, and if the player was AFK,
     * ends that AFK session.
     */
    private void markActive(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (afkSince.containsKey(player.getUniqueId())) {
            setAfk(player, false);
        }
    }

    /**
     * The only place AFK state actually changes. Handles the chat message and rolls the finished
     * session's duration into the persisted total.
     */
    private void setAfk(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        boolean currentlyAfk = afkSince.containsKey(uuid);
        if (afk == currentlyAfk) return;

        if (afk) {
            afkSince.put(uuid, System.currentTimeMillis());
            player.sendMessage(nowAfkMessage);
        } else {
            Long since = afkSince.remove(uuid);
            if (since != null) {
                long seconds = (System.currentTimeMillis() - since) / 1000;
                totalAfkSeconds.merge(uuid, seconds, Long::sum);
            }
            player.sendMessage(noLongerAfkMessage);
        }
    }

    /**
     * Runs once a second. Anyone online, not already AFK, whose last activity was over a minute ago
     * gets flagged.
     */
    private void checkAfkTimeouts() {
        if (!afkEnabled) return;
        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            if (afkSince.containsKey(uuid)) continue;
            long last = lastActivity.getOrDefault(uuid, now);
            if (now - last >= afkThresholdMs) {
                setAfk(p, true);
            }
        }
    }

    private void loadAfkData() {
        afkDataFile = new File(getDataFolder(), "afkdata.yml");
        if (!afkDataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(afkDataFile);
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                totalAfkSeconds.put(uuid, section.getLong(key));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed entries from older data files.
            }
        }
    }

    private void saveAfkData() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : totalAfkSeconds.entrySet()) {
            config.set("players." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(afkDataFile);
        } catch (IOException e) {
            getLogger().warning("Could not save AFK data: " + e.getMessage());
        }
    }

    /**
     * Total AFK minutes for the leaderboard. Live for whoever is currently AFK right now (includes
     * their in-progress session, same reasoning as the live advancement count), persisted total
     * otherwise.
     */
    private int getTotalAfkMinutes(OfflinePlayer target) {
        UUID uuid = target.getUniqueId();
        long seconds = totalAfkSeconds.getOrDefault(uuid, 0L);
        Long since = afkSince.get(uuid);
        if (since != null) {
            seconds += (System.currentTimeMillis() - since) / 1000;
        }
        return (int) (seconds / 60);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());
        totalAfkSeconds.putIfAbsent(uuid, 0L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Long since = afkSince.remove(uuid);
        if (since != null) {
            long seconds = (System.currentTimeMillis() - since) / 1000;
            totalAfkSeconds.merge(uuid, seconds, Long::sum);
        }
        lastActivity.remove(uuid);
        recordingPlayers.remove(uuid);
        livePlayers.remove(uuid);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;

        // Camera rotation alone does not reset the AFK timer.
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            markActive(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        markActive(e.getPlayer());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        markActive(e.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        markActive(e.getPlayer());
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent e) {
        // Let /afk toggle status without first clearing it through command activity.
        if (e.getMessage().toLowerCase().startsWith("/afk")) return;
        markActive(e.getPlayer());
    }

    private void updateStatsCache() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSkinCacheClear > skinCacheDurationMs) {
            headTextureCache.clear();
            lastSkinCacheClear = currentTime;
            getLogger().info("Cleared player head cache (head-cache-clear-hours passed)");
        }

        if (currentTime - lastCacheUpdate < cacheDurationMs) {
            return;
        }

        Map<String, List<PlayerStat>> newCache = new HashMap<>();
        List<PlayerStat> killStats = new ArrayList<>();
        List<PlayerStat> deathStats = new ArrayList<>();
        List<PlayerStat> playtimeStats = new ArrayList<>();
        List<PlayerStat> advancementStats = new ArrayList<>();
        List<PlayerStat> distanceStats = new ArrayList<>();
        List<PlayerStat> afkStats = new ArrayList<>();

        int totalAdvancements = getTotalAdvancementCount();

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (!offlinePlayer.hasPlayedBefore()) {
                continue;
            }

            if (offlinePlayer.getName() == null) {
                continue;
            }

            try {
                int kills = offlinePlayer.getStatistic(Statistic.PLAYER_KILLS);
                int deaths = offlinePlayer.getStatistic(Statistic.DEATHS);
                int playtime = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;

                if (kills > 0) killStats.add(new PlayerStat(offlinePlayer, kills));
                if (deaths > 0) deathStats.add(new PlayerStat(offlinePlayer, deaths));
                if (playtime > 0) playtimeStats.add(new PlayerStat(offlinePlayer, playtime));
            } catch (Exception e) {
                // Skip offline profiles whose statistics cannot be read.
            }

            int advancements = getAdvancementCount(offlinePlayer);
            if (advancements > 0) {
                advancementStats.add(new PlayerStat(offlinePlayer, advancements));
            }

            int distance = getDistanceTraveled(offlinePlayer);
            if (distance > 0) {
                distanceStats.add(new PlayerStat(offlinePlayer, distance));
            }

            int afkMinutes = getTotalAfkMinutes(offlinePlayer);
            if (afkMinutes > 0) {
                afkStats.add(new PlayerStat(offlinePlayer, afkMinutes));
            }
        }

        killStats.sort((a, b) -> Integer.compare(b.value, a.value));
        deathStats.sort((a, b) -> Integer.compare(b.value, a.value));
        playtimeStats.sort((a, b) -> Integer.compare(b.value, a.value));
        advancementStats.sort((a, b) -> Integer.compare(b.value, a.value));
        distanceStats.sort((a, b) -> Integer.compare(b.value, a.value));
        afkStats.sort((a, b) -> Integer.compare(b.value, a.value));

        newCache.put("Kills", killStats);
        newCache.put("Deaths", deathStats);
        newCache.put("Playtime", playtimeStats);
        newCache.put("Advancements", advancementStats);
        newCache.put("Distance Traveled", distanceStats);
        newCache.put("AFK Time", afkStats);

        this.totalAdvancementCount = totalAdvancements;

        this.cachedStats = newCache;
        this.lastCacheUpdate = currentTime;
    }

    /**
     * NEW: counts a player's completed advancements.
     *
     * <p>Bukkit's OfflinePlayer has no advancement API (only the online Player interface exposes
     * getAdvancementProgress), so for full offline support - matching how Kills/Deaths/Playtime
     * work for every player who's ever joined, not just those online right now - offline players
     * are counted by reading the vanilla per-player advancements file the server already maintains
     * (see findAdvancementsDir for where that is).
     *
     * <p>A player who is CURRENTLY ONLINE is checked live instead (see countAdvancementsLive
     * below), because that on-disk file is only written out periodically/on autosave/on logout -
     * reading it for an online player can show a stale count for a while after they earn a new
     * advancement, which is exactly the "stuck" number you'd see if you check your own stats right
     * after getting one.
     */
    private int getAdvancementCount(OfflinePlayer target) {
        Player online = target.getPlayer();
        if (online != null) {
            return countAdvancementsLive(online);
        }
        return countAdvancementsFromFile(target);
    }

    /**
     * An advancement only counts as "real" (something a player actually earns and would recognize)
     * if it has display info - a title, icon, description. Crafting recipes are registered as
     * advancements purely for recipe-book bookkeeping and have no display info at all, and the same
     * is true of "tracking" advancements some datapacks use internally (e.g. to trigger a function
     * or flag scripted progress) - they're never meant to be seen, so they're built without a
     * display block too. This one check quietly excludes both.
     */
    private boolean isRealAdvancement(Advancement advancement) {
        return advancement.getDisplay() != null;
    }

    /** Live, always-up-to-date count for a player who is currently online. */
    private int countAdvancementsLive(Player player) {
        int count = 0;
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            if (!isRealAdvancement(advancement)) continue;
            if (player.getAdvancementProgress(advancement).isDone()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Folder holding the vanilla per-player advancement files, or null if there isn't one yet (e.g.
     * nobody has ever joined).
     *
     * <p>Up to 1.21.x this was <world>/advancements. Since 26.1 the player files live in
     * <level>/players/advancements, and the main world's own folder is
     * <level>/dimensions/minecraft/overworld - so instead of hard-coding either layout, walk up
     * from the world folder until one of them exists.
     */
    private File findAdvancementsDir() {
        File dir = Bukkit.getWorlds().get(0).getWorldFolder();
        for (int i = 0; i < 4 && dir != null; i++) {
            File modern = new File(new File(dir, "players"), "advancements");
            if (modern.isDirectory()) {
                return modern;
            }
            File legacy = new File(dir, "advancements");
            if (legacy.isDirectory()) {
                return legacy;
            }
            dir = dir.getParentFile();
        }
        return null;
    }

    /**
     * Fallback count for a player who is NOT currently online, read straight from the vanilla
     * advancements file. Each key is cross-referenced against the server's live advancement
     * registry (same isRealAdvancement check as the online path) so recipes and hidden tracking
     * advancements are excluded here too, instead of the number ballooning into the thousands or
     * counting things the player never actually "earned".
     */
    private int countAdvancementsFromFile(OfflinePlayer target) {
        File advancementsDir = findAdvancementsDir();
        if (advancementsDir == null) {
            return 0;
        }
        File file = new File(advancementsDir, target.getUniqueId() + ".json");
        if (!file.exists()) {
            return 0;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            int count = 0;
            for (String key : json.keySet()) {
                if (key.equals("DataVersion")) continue;

                JsonElement element = json.get(key);
                if (!element.isJsonObject()) continue;
                JsonObject entry = element.getAsJsonObject();
                if (!entry.has("done") || !entry.get("done").getAsBoolean()) continue;

                NamespacedKey namespacedKey = NamespacedKey.fromString(key);
                if (namespacedKey == null) continue;
                Advancement advancement = Bukkit.getAdvancement(namespacedKey);
                if (advancement == null || !isRealAdvancement(advancement)) continue;

                count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Total number of real advancements registered on the server (vanilla + datapack-added), using
     * the same isRealAdvancement check as the per-player counts - so a custom advancement like
     * "Completionist" is included, but recipes and datapack tracking advancements aren't.
     */
    private int getTotalAdvancementCount() {
        int count = 0;
        Iterator<Advancement> it = Bukkit.advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            if (isRealAdvancement(advancement)) count++;
        }
        return count;
    }

    /**
     * Total distance a player has covered under their own steam, in blocks. Sums every real form of
     * player-driven movement (walking, sprinting, swimming, boats, minecarts, horses, pigs,
     * striders, climbing, and elytra flying). Deliberately leaves out FALL_ONE_CM (falling isn't
     * "traveling") and FLY_ONE_CM (creative/spectator flight - including it would let staff or
     * creative-mode players dominate a leaderboard that's supposed to be about survival travel).
     */
    private int getDistanceTraveled(OfflinePlayer target) {
        try {
            long totalCm = 0;
            totalCm += target.getStatistic(Statistic.WALK_ONE_CM);
            totalCm += target.getStatistic(Statistic.SPRINT_ONE_CM);
            totalCm += target.getStatistic(Statistic.SWIM_ONE_CM);
            totalCm += target.getStatistic(Statistic.WALK_ON_WATER_ONE_CM);
            totalCm += target.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM);
            totalCm += target.getStatistic(Statistic.CLIMB_ONE_CM);
            totalCm += target.getStatistic(Statistic.BOAT_ONE_CM);
            totalCm += target.getStatistic(Statistic.MINECART_ONE_CM);
            totalCm += target.getStatistic(Statistic.HORSE_ONE_CM);
            totalCm += target.getStatistic(Statistic.PIG_ONE_CM);
            totalCm += target.getStatistic(Statistic.STRIDER_ONE_CM);
            totalCm += target.getStatistic(Statistic.AVIATE_ONE_CM);
            return (int) (totalCm / 100); // Minecraft tracks these in centimeters; 100cm = 1 block
        } catch (Exception e) {
            return 0;
        }
    }

    private void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, mainMenuSize, mainMenuTitle);

        for (MenuItemConfig item : menuItems.values()) {
            if (!item.enabled) continue;

            ItemStack stack = new ItemStack(item.material);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(item.displayName);
            meta.setLore(item.lore);
            stack.setItemMeta(meta);

            inv.setItem(item.slot, stack);
        }

        player.openInventory(inv);
    }

    private void openStatsMenu(Player player, String type, int page) {
        currentPage.put(player.getUniqueId(), page);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateStatsCache();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        buildStatsInventory(player, type, page);
                    }
                }.runTask(StatsPlugin.this);
            }
        }.runTaskAsynchronously(this);

        showLoadingMenu(player, type);
    }

    private void showLoadingMenu(Player player, String type) {
        Inventory inv =
                Bukkit.createInventory(null, 54, "§6§l" + shortenForTitle(type) + " Leaderboard");

        ItemStack loading = new ItemStack(Material.HOPPER);
        ItemMeta loadingMeta = loading.getItemMeta();
        loadingMeta.setDisplayName("§e§lLoading...");
        loadingMeta.setLore(Arrays.asList("§7Please wait"));
        loading.setItemMeta(loadingMeta);

        inv.setItem(22, loading);
        player.openInventory(inv);
    }

    private void buildStatsInventory(Player player, String type, int page) {
        Inventory inv =
                Bukkit.createInventory(null, 54, "§6§l" + shortenForTitle(type) + " Leaderboard");

        List<PlayerStat> stats = cachedStats.getOrDefault(type, new ArrayList<>());
        int totalPages = Math.max(1, (int) Math.ceil((double) stats.size() / itemsPerPage));
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, stats.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            PlayerStat stat = stats.get(i);
            ItemStack head = getCachedHead(stat.player);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName("§e#" + (i + 1) + " §f" + stat.player.getName());

            List<String> lore = new ArrayList<>();
            if (type.equals("Playtime")) {
                lore.add("§7" + type + ": §a" + formatPlaytime(stat.value));
                lore.add("§7Last login: §a" + formatLastLogin(stat.player));
            } else if (type.equals("AFK Time")) {
                lore.add("§7" + type + ": §a" + formatPlaytime(stat.value));
            } else if (type.equals("Advancements")) {
                lore.add("§7" + type + ": §a" + stat.value + "§7/§a" + totalAdvancementCount);
            } else if (type.equals("Distance Traveled")) {
                lore.add("§7" + type + ": §a" + formatDistance(stat.value));
            } else {
                lore.add("§7" + type + ": §a" + stat.value);
            }
            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot++, head);
        }

        if (page > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName("§e§l← Previous Page");
            prevMeta.setLore(Arrays.asList("§7Page " + page + " of " + totalPages));
            prevPage.setItemMeta(prevMeta);
            inv.setItem(48, prevPage);
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§c§lBack to Menu");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        if (page < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName("§e§lNext Page →");
            nextMeta.setLore(Arrays.asList("§7Page " + (page + 2) + " of " + totalPages));
            nextPage.setItemMeta(nextMeta);
            inv.setItem(50, nextPage);
        }

        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageIndicator.getItemMeta();
        pageMeta.setDisplayName("§6§lPage " + (page + 1) + "/" + totalPages);
        pageMeta.setLore(
                Arrays.asList(
                        "§7Showing "
                                + (endIndex - startIndex)
                                + " of "
                                + stats.size()
                                + " players"));
        pageIndicator.setItemMeta(pageMeta);
        inv.setItem(53, pageIndicator);

        player.openInventory(inv);
    }

    private ItemStack getCachedHead(OfflinePlayer target) {
        ItemStack cached = headTextureCache.get(target.getUniqueId());
        if (cached != null) {
            return cached.clone();
        }

        ItemStack newHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) newHead.getItemMeta();
        meta.setOwningPlayer(target);
        newHead.setItemMeta(meta);

        headTextureCache.put(target.getUniqueId(), newHead);
        return newHead.clone();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        markActive(player); // clicking in any inventory counts as real activity
        String title = e.getView().getTitle();

        if (title.equals(mainMenuTitle)) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null
                    || e.getClickedInventory() != e.getView().getTopInventory()) return;
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            String type = getTypeForSlot(e.getSlot());
            if (type != null) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                openStatsMenu(player, type, 0);
            }
        } else if (title.contains("Leaderboard")) {
            e.setCancelled(true);
            if (e.getClickedInventory() == null
                    || e.getClickedInventory() != e.getView().getTopInventory()) return;
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            ItemStack clicked = e.getCurrentItem();
            Material type = clicked.getType();

            if (type == Material.BARRIER) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                openMainMenu(player);
                currentPage.remove(player.getUniqueId());
            } else if (type == Material.ARROW) {
                player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                String displayName = clicked.getItemMeta().getDisplayName();
                String statType =
                        unshortenFromTitle(title.replace("§6§l", "").replace(" Leaderboard", ""));
                int currentPlayerPage = currentPage.getOrDefault(player.getUniqueId(), 0);

                if (displayName.contains("Previous")) {
                    openStatsMenu(player, statType, currentPlayerPage - 1);
                } else if (displayName.contains("Next")) {
                    openStatsMenu(player, statType, currentPlayerPage + 1);
                }
            }
        }
    }

    private String formatPlaytime(int totalMinutes) {
        int days = totalMinutes / 1440;
        int hours = (totalMinutes % 1440) / 60;
        int minutes = totalMinutes % 60;

        StringBuilder formatted = new StringBuilder();
        if (days > 0) {
            formatted.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            formatted.append(hours).append("h ");
        }
        formatted.append(minutes).append("m");

        return formatted.toString().trim();
    }

    private String formatDistance(int blocks) {
        if (blocks >= 1000) {
            return String.format("%.1f km", blocks / 1000.0);
        }
        return blocks + " blocks";
    }

    /**
     * Human-friendly "last login" text for a player. Sourced from OfflinePlayer.getLastLogin() -
     * vanilla Minecraft's own per-player data, maintained since their very first join, completely
     * independent of this plugin. No migration or log-parsing needed for existing players; it's
     * already there for everyone.
     *
     * <p>Falls back to the older, deprecated getLastPlayed() if getLastLogin() comes back empty (0)
     * - some players' data (typically anyone who hasn't logged in since long before this plugin's
     * install) has a real, valid "lastPlayed" entry that getLastLogin() doesn't appear to pick up,
     * but getLastPlayed() still does. The deprecation warning is suppressed deliberately - this is
     * a known, intentional fallback, not an oversight.
     */
    @SuppressWarnings("deprecation")
    private String formatLastLogin(OfflinePlayer target) {
        if (target.isOnline()) {
            return "Online now";
        }

        long lastLogin = target.getLastLogin();
        if (lastLogin <= 0) {
            lastLogin = target.getLastPlayed();
        }
        if (lastLogin <= 0) {
            return "Unknown";
        }

        long diffMs = System.currentTimeMillis() - lastLogin;
        long minutes = diffMs / 60_000L;
        long hours = minutes / 60L;
        long days = hours / 24L;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        if (days < 30) return days + (days == 1 ? " day ago" : " days ago");

        // Use a date for older activity.
        return new java.text.SimpleDateFormat("MMM d, yyyy").format(new java.util.Date(lastLogin));
    }

    /**
     * The chest GUI's title bar only has so many pixels before bold text gets visually cut
     * off/pushed outside the frame. All the other stat names are one word ("Kills Leaderboard",
     * etc), but "Distance Traveled" isn't, so just the title text gets shortened here. The
     * underlying type string is untouched everywhere else (cache key, lore, click routing) - see
     * unshortenFromTitle below for why that matters.
     */
    private String shortenForTitle(String type) {
        if (type.equals("Distance Traveled")) {
            return "Distance";
        }
        return type;
    }

    /**
     * Reverses shortenForTitle(). The Previous/Next page buttons figure out which leaderboard
     * they're on by parsing the stat type back out of the inventory title, so if the title was
     * shortened, this needs to map it back to the real type - otherwise pagination would look up
     * the wrong (or a nonexistent) key in the stats cache.
     */
    private String unshortenFromTitle(String shortType) {
        if (shortType.equals("Distance")) {
            return "Distance Traveled";
        }
        return shortType;
    }

    private static class PlayerStat {
        OfflinePlayer player;
        int value;

        PlayerStat(OfflinePlayer player, int value) {
            this.player = player;
            this.value = value;
        }
    }

    /**
     * One main-menu item's config.yml-driven settings: whether it's shown, which slot it occupies,
     * its icon, display name, and lore.
     */
    private static class MenuItemConfig {
        boolean enabled;
        int slot;
        Material material;
        String displayName;
        List<String> lore;
    }
}
