# Build tools

`build_plugins.py` builds the four active custom plugins. Use Java 25 and Python 3.10 or later.

```sh
python Tools/build_plugins.py --server-dir /path/to/paper --plugin-dir /path/to/paper/plugins --output-dir Releases/Plugins/current
```

Pass a plugin name before the options to build only that project: `SpawnGuard`, `StatsPlugin`, `PlaceableDecor` or `FinalStationMap`.

The Paper folder must contain extracted `libraries/` and `versions/` directories from the matching server. The local development default is `test/pikmin-build`. StatsPlugin also requires a PlaceholderAPI JAR under `--plugin-dir`. JetBrains annotations are read from the Maven cache or downloaded as a checksum-pinned compile dependency.

The map build downloads pinned upstream dependencies into `.cache/dependencies/` and checks their SHA-256 hashes before use. Builds go to each project's `target/` folder. Release output and dependency caches are ignored by Git.

`build.py` at the repository root packages the maintained datapacks, resource pack and Skripts. It excludes third-party datapack archive collections.
