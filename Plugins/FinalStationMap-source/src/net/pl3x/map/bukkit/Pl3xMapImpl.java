package net.pl3x.map.bukkit;

import com.google.common.collect.ImmutableList;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.world.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

/**
 * Final Station patch for Minecraft 26.3. Two things changed in the server: - RedStoneWireBlock was
 * renamed to RedstoneWireBlock - world generation "configured features" no longer exist; a feature
 * now carries its own settings (ConfiguredFeature + SimpleBlockConfiguration became Feature /
 * SimpleBlockFeature) Everything else in this class is unchanged from Pl3xMap 26.2-555.
 */
@NullMarked
public class Pl3xMapImpl extends Pl3xMap {
    private final RandomSource randomSource = RandomSource.createThreadSafe();
    private final JavaPlugin plugin;
    private BukkitAudiences adventure;
    private Path jarPath;
    private Map<Biome, List<Feature>> biomeFeatureCache = new LinkedHashMap<>();
    ArrayList<ResourceKey<Feature>> canSpawnFromBonemealList = new ArrayList<>(10);

    public Pl3xMapImpl(JavaPlugin plugin) {
        super(true);
        this.plugin = plugin;
    }

    public void enable() {
        this.adventure = BukkitAudiences.create(this.plugin);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_DEFAULT);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_FLOWER_FOREST);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_SWAMP);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_PLAIN);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_MEADOW);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_CHERRY);
        this.canSpawnFromBonemealList.add(VegetationFeatures.WILDFLOWER);
        this.canSpawnFromBonemealList.add(VegetationFeatures.FLOWER_PALE_GARDEN);
        super.enable();
    }

    public void disable() {
        super.disable();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public String getPlatform() {
        String name = Bukkit.getName();
        if ("CraftBukkit".equals(name)) {
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                return "spigot";
            } catch (Throwable var3) {
            }
        }

        return name.toLowerCase(Locale.ROOT);
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    public boolean getOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    public String getServerVersion() {
        return Bukkit.getVersion();
    }

    public AudienceProvider adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        } else {
            return this.adventure;
        }
    }

    public Path getMainDir() {
        return this.plugin.getDataFolder().toPath();
    }

    public Path getJarPath() {
        if (this.jarPath == null) {
            try {
                this.jarPath =
                        Path.of(
                                Pl3xMap.class
                                        .getProtectionDomain()
                                        .getCodeSource()
                                        .getLocation()
                                        .toURI());
            } catch (URISyntaxException var2) {
                throw new RuntimeException(var2);
            }
        }

        return this.jarPath;
    }

    public int getColorForPower(byte power) {
        return RedstoneWireBlock.getColorForPower(Math.max(0, Math.min(15, power)));
    }

    private List<Feature> getBoneMealFeatures(Biome biome) {
        if (!this.biomeFeatureCache.containsKey(biome)) {
            ImmutableList.Builder<Feature> flowers = ImmutableList.builder();
            for (HolderSet<PlacedFeature> step : biome.getGenerationSettings().features()) {
                for (Holder<PlacedFeature> placed : step) {
                    placed.value()
                            .getFeatures()
                            .forEach(
                                    feature -> {
                                        Optional<ResourceKey<Feature>> key = feature.unwrapKey();
                                        if (key.isPresent()
                                                && this.canSpawnFromBonemealList.contains(
                                                        key.get())) {
                                            flowers.add(feature.value());
                                        }
                                    });
                }
            }
            this.biomeFeatureCache.put(biome, flowers.build());
        }

        return this.biomeFeatureCache.get(biome);
    }

    public net.pl3x.map.core.world.@Nullable Block getFlower(
            World world, net.pl3x.map.core.world.Biome biome, int blockX, int blockY, int blockZ) {
        Biome nms =
                (Biome)
                        ((ServerLevel) world.getLevel())
                                .registryAccess()
                                .lookupOrThrow(Registries.BIOME)
                                .getValue(Identifier.parse(biome.getKey()));
        if (nms == null) {
            return null;
        } else {
            List<Feature> flowers = this.getBoneMealFeatures(nms);
            if (flowers.isEmpty()
                    || !(flowers.getFirst() instanceof SimpleBlockFeature flowerMap)) {
                return null;
            } else {
                Block block =
                        flowerMap
                                .toPlace()
                                .value()
                                .getState(
                                        (LevelAccessor) world.getLevel(),
                                        this.randomSource,
                                        new BlockPos(blockX, blockY, blockZ))
                                .getBlock();
                return this.getBlockRegistry()
                        .get(BuiltInRegistries.BLOCK.getKey(block).toString());
            }
        }
    }

    protected void loadBlocks() {
        for (Entry<ResourceKey<Block>, Block> entry :
                MinecraftServer.getServer()
                        .registryAccess()
                        .lookupOrThrow(Registries.BLOCK)
                        .entrySet()) {
            String id = entry.getKey().identifier().toString();
            int color = entry.getValue().defaultMapColor().col;
            this.getBlockRegistry().register(id, color);
        }

        this.getBlockRegistry().saveToDisk();
    }

    protected void loadWorlds() {
        Bukkit.getWorlds()
                .forEach(
                        world ->
                                this.getWorldRegistry()
                                        .getOrDefault(
                                                world.getName(),
                                                () ->
                                                        new BukkitWorld(
                                                                ((CraftWorld) world).getHandle(),
                                                                world.getName())));
    }

    protected void loadPlayers() {
        Bukkit.getOnlinePlayers()
                .forEach(
                        player -> {
                            UUID uuid = player.getUniqueId();
                            this.getPlayerRegistry()
                                    .getOrDefault(uuid, () -> new BukkitPlayer(player));
                        });
    }

    public World cloneWorld(World world) {
        return new BukkitWorld((ServerLevel) world.getLevel(), world.getName());
    }
}
