# Server comparison: 28 October 2026

Compared the supplied `Server Copy - plugins` and `Server Copy - Datapacks` snapshots with local runtime copies and maintained sources. Snapshots are ignored by Git. No server-copy files were overwritten.

Comparisons describe the files before this cleanup. Subsequent formatting and README additions are intentional local changes. The snapshots do not include the deployed resource pack or root Paper configuration, so those could not be compared with the server.

## Plugin JARs

33 of 35 deployed plugin JARs were byte-for-byte identical to the local 26.3 collection. Geyser and Floodgate exist only in the server snapshot.

| Plugin | Server version | Local comparison |
| --- | --- | --- |
| AxGraves | 1.32.1 | Identical JAR |
| AxiomPaper | 6.0.1+26.3 | Identical JAR |
| Chunky | 1.5.3 | Identical JAR |
| CommandAPI | 12.1.0 | Identical JAR |
| CustomDiscs | 2.7.0 | Identical JAR |
| DiscordSRV | 1.30.5 | Identical JAR |
| ElytraDisabled | 1.2.2 | Identical JAR |
| floodgate | 2.2.5-SNAPSHOT (b141-81b65cc) | Server copy only |
| Geyser-Spigot | 2.12.0-SNAPSHOT | Server copy only |
| ImageFrame | 2026.1.5.0 | Identical JAR |
| InventoryRollbackPlus | 1.8.5 | Identical JAR |
| InvSeePlusPlus | 0.31.19 | Identical JAR |
| LuckPerms | 5.5.71 | Identical JAR |
| MiniMOTD | 2.2.5 | Identical JAR |
| Multiverse-Core | 5.8.1 | Identical JAR |
| Multiverse-Inventories | 5.3.6 | Identical JAR |
| Multiverse-NetherPortals | 5.1.0 | Identical JAR |
| Multiverse-Portals | 5.3.0 | Identical JAR |
| packetevents | 2.14.0 | Identical JAR |
| Pl3xMap | 26.3-555-finalstation-3 | Identical JAR |
| PlaceableDecor | 1.1.0 | Identical JAR |
| PlaceholderAPI | 2.12.3 | Identical JAR |
| playerSit | 1.2.0 | Identical JAR |
| SkBee | 3.26.0 | Identical JAR |
| SkinsRestorer | 15.12.6 | Identical JAR |
| Skript | 2.17.0-pre1 | Identical JAR |
| SpawnGuard | 1.3 | Identical JAR |
| StatsPlugin | 1.8 | Identical JAR |
| TAB | 6.2.0 | Identical JAR |
| TakeItOutPlugin | 1.14 | Identical JAR |
| ViaBackwards | 5.12.0 | Identical JAR |
| ViaVersion | 5.12.0 | Identical JAR |
| voicechat | 2.6.24 | Identical JAR |
| WorldGuard | 7.0.19+2400-f395a16 | Identical JAR |
| Worlds | 4.5.0-pre1 | Identical JAR |

The extra local Pl3xMap `finalstation-2` JAR is an older duplicate; the server uses `finalstation-3`. Older 1.21.10 collections are not release inputs.

## Maintained datapacks

| Source | Server ZIP | Comparison |
| --- | --- | --- |
| all-achievements | all-achievements-2.0.0.zip | Identical contents |
| custom-paintings | custom-paintings-1.0.0.zip | Identical contents |
| final-station-crafting | final-station-crafting-1.0.0.zip | Identical contents |
| final-station-guide | final-station-guide-1.1.0.zip | Identical contents |
| murder-mystery | murder-mystery-1.8.0.zip | Identical contents |
| painting-picker | painting-picker-2.0.0.zip | Identical contents |
| seedling-pikmin | Seedling_Halloween.zip | See Seedling notes |
| seedling-pikmin | Seedling_Pikmin_Datapack_Halloween.zip | See Seedling notes |
| smelted-leather | smelted-leather-1.0.0.zip | Identical contents |

### Seedling overlap

`Seedling_Halloween.zip` matches the current Seedling commands and data; only `pack.mcmeta` and the archive README differ. `Seedling_Pikmin_Datapack_Halloween.zip` is older: it lacks 17 current functions and has different load/tick functions. Both contain the same namespace and overlapping resources. Keep one intended version when deploying, after checking the active datapack order. Neither snapshot ZIP was deleted.

### Third-party packs

These server packs are not maintained as unpacked source in this repository:

- `afk display v1.1.18 (MC 26.3).zip`
- `armor statues v2.8.24 (MC 26.3).zip`
- `better-cutter-v2.3.0.1.zip`
- `biomePowder-1.4-mc26.3.zip`
- `cauldron concrete v3.1.2 (MC 26.3).zip`
- `cauldron mud v1.0.11 (MC 26.3).zip`
- `Cauldron_Potion_26.3.zip`
- `craft-music-discs-v2.1.0.0.zip`
- `double shulker shells v1.3.18 (MC 26.3).zip`
- `HorseArmorCraft-v2.88.0-mc26.3.zip`
- `Invisible Item Frame-mc26.3.zip`
- `lapis_iron_golem_26.3_lightning_effects.zip`
- `leash-fences-v1.2.0.0.zip`
- `more effective tools v1.0.12 (MC 26.3).zip`
- `more recipes.undo.polished stone 5.6.0-beta-mc26.3.zip`
- `too-expensive-removed-v1.3.0.0.zip`
- `wizard_15_spells_26.3.zip`

## Runtime configuration and data

Most folder differences are player databases, map tiles, cached skins, inventory backups and Skript variables. These remain ignored and are not source release assets. Configuration differences were found in CustomDiscs, Multiverse world definitions, Pl3xMap settings/player layer and live plugin defaults. Live values were not copied over source defaults.

The server snapshot already has both Pl3xMap forced-GC options disabled. The local GC change from the earlier performance investigation therefore does not need deploying from this snapshot.

## Source and release cleanup

- Four active custom plugin projects now use unversioned `Name-source` folder names.
- Rebuilt SpawnGuard, StatsPlugin, PlaceableDecor and map patch classes match deployed bytecode after formatting. SpawnGuard's local source reload message color code was restored to match the deployed JAR.
- The map source was missing the deployed default-property fallback and redstone-power clamp. Both fixes were recovered from the deployed build before rebuilding.
- The map build now compiles source and uses pinned upstream dependencies instead of stored compiled overlays.
- Formatting-only differences from this cleanup do not represent deployment drift.
- Release JARs, checksums and source archives are kept under ignored `Releases/Plugins/`.

## Skripts

- `ban.sk`: Identical.
- `height.sk`: Identical.
- `ranks.sk`: Identical.
- `restart-broadcast.sk`: Identical.
- `server-start.sk`: Identical.
- `spells.sk`: Identical.
- `welcome.sk`: Identical.
- `whitelist.sk`: Identical.
- `worlds.sk`: Identical.
