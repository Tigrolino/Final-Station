# Final Station Map

A Paper 26.3 port of Pl3xMap 26.2-555 with Final Station web branding. This is a modified upstream build, not a separate map plugin.

## Install

Requires Paper 26.3 and Java 25. Replace the old map JAR with `Pl3xMap-26.3-555-finalstation-3.jar` and restart. Keep only one Pl3xMap JAR installed.

The plugin name, `/map` commands, permissions and `plugins/Pl3xMap` data folder stay unchanged. Existing map tiles and add-ons remain in that folder.

## Changes

- Reads Minecraft 26.3 block palettes, including plain and wrapped block names.
- Supplies missing age, moisture and power properties when needed by the renderer.
- Updates renamed Minecraft classes and world-generation APIs; clamps redstone power before color lookup.
- Adds the 26.3 blocks and translations.
- Uses cloud-bukkit 2.0.1's item parser for 26.3 command compatibility.
- Applies the map title, page metadata, banner and favicon for The Final Station.

The web metadata contains the public map address. Update `overlay/web/index.html` when that address changes. Runtime rendering and web-server options remain in Pl3xMap's own config.

## Build

```sh
python Tools/build_plugins.py FinalStationMap --server-dir /path/to/paper
```

Run from the repository root with Java 25 and Python 3.10 or later. The build tool downloads checksum-pinned Pl3xMap and cloud-bukkit dependencies, compiles the changed classes, patches the title and applies the web assets. Compiled overlays are not stored in Git.

The output is `target/Pl3xMap-26.3-555-finalstation-3.jar`. `tools/LinkCheck.java` checks references against the target Paper server when updating the port.

## Upstream

Pl3xMap is maintained by granny, BillyGalbreath and JLyne. See [Pl3xMap](https://modrinth.com/plugin/pl3xmap) and [NOTICE.md](NOTICE.md) for attribution and the parser's license.
