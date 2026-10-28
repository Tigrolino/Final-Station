<p align="center">
  <img src="final-station-banner.jpg" alt="The Final Station banner" width="100%">
</p>

# The Final Station

Custom plugin sources, datapacks, Skripts and the resource pack for The Final Station Minecraft server. The current server runs Paper 26.3 with Java 25.

## Layout

| Folder | Contents |
| --- | --- |
| `Plugins/` | Active custom plugin projects; see [Plugins/README.md](Plugins/README.md) |
| `Datapacks/` | Maintained unpacked datapacks |
| `Resourcepack/` | Server resource pack |
| `Skripts/` | Server scripts, installed under `plugins/Skript/scripts/` |
| `Tools/` | Custom plugin build tooling |
| `Docs/` | Server comparison and release notes |

Server configuration (`Config/`), server copies, third-party plugin JARs, old versions, Discord assets, worlds, reference resources, scratch tests and release output are local-only. They are excluded through `.gitignore`.

## Custom plugins

| Plugin | Purpose |
| --- | --- |
| [SpawnGuard](Plugins/SpawnGuard-source/README.md) | Spawn protection, vine growth and mob block-damage rules |
| [StatsPlugin](Plugins/StatsPlugin-source/README.md) | Statistics menus, leaderboards and player status tags |
| [PlaceableDecor](Plugins/PlaceableDecor-source/README.md) | Configurable collectible decorations, including Pikmin and hats |
| [Final Station Map](Plugins/FinalStationMap-source/README.md) | Pl3xMap port and web branding |

Build instructions are in [Tools/README.md](Tools/README.md). JARs and source bundles are attached to GitHub Releases rather than committed to the repository.

## Datapacks

| Folder | Purpose |
| --- | --- |
| `all-achievements` | Completionist advancement |
| `custom-paintings` | Custom paintings used by the resource pack |
| [final-station-crafting](Datapacks/final-station-crafting/README.md) | Custom recipes |
| `final-station-guide` | Traveller's Guide book and updates |
| [murder-mystery](Datapacks/murder-mystery/README.md) | Murder mystery event |
| `painting-picker` | Painting selection through a stonecutter |
| `seedling-pikmin` | Seedlings, Pikmin rewards and cauldron recipes |
| `smelted-leather` | Rotten flesh to leather recipes |

Third-party archive collections are kept locally. The [server comparison](Docs/server-comparison-2026-10-28.md) lists differences and overlapping deployed packs.

## Skripts and resource pack

Skript files start with their commands, permissions and dependencies. They cover ranks, worlds, whitelist management, height, announcements and server events. Some require LuckPerms, Multiverse, Geyser or Floodgate.

The resource pack contains custom hats, staffs, railings, paintings, guide-book artwork and Seedling/Pikmin models. Images used by the pack remain under `Resourcepack/assets/`; the separate `Resources/` folder contains local reference originals.

## Style and credits

See [STYLE.md](STYLE.md) for formatting and project layout. Keep generated files and private server data out of commits.

Final Station work by Tigrolino and The3dGamerz. StatsPlugin originated with StarMaster. Pl3xMap and the included parser retain their upstream attribution. Third-party datapacks retain their authors' metadata.

The repository's own work is covered by [LICENSE](LICENSE). Upstream components retain their own licenses.
