# Custom plugins

These are the maintained plugin sources for The Final Station. All target Paper 26.3 and Java 25.

| Project | Server plugin name | Release JAR |
| --- | --- | --- |
| [SpawnGuard](SpawnGuard-source/README.md) | SpawnGuard | `SpawnGuard-1.3.jar` |
| [StatsPlugin](StatsPlugin-source/README.md) | StatsPlugin | `StatisticsViewer-1.8.jar` |
| [PlaceableDecor](PlaceableDecor-source/README.md) | PlaceableDecor | `PlaceableDecor-1.1.0.jar` |
| [Final Station Map](FinalStationMap-source/README.md) | Pl3xMap | `Pl3xMap-26.3-555-finalstation-3.jar` |

Source folders do not include version numbers. Runtime JARs, downloaded dependencies, old versions and plugin data are ignored by Git.

Build all projects from the repository root:

```sh
python Tools/build_plugins.py --server-dir /path/to/paper --plugin-dir /path/to/paper/plugins
```

See [the build tools](../Tools/README.md) for dependency setup. Release assets are stored under the ignored `Releases/Plugins/` folder and uploaded to GitHub Releases.
