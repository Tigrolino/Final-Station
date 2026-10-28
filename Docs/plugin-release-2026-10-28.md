# Custom plugins: 28 October 2026

Maintained sources and rebuilt JARs for Paper 26.3 and Java 25.

| Plugin | Version |
| --- | --- |
| SpawnGuard | 1.3 |
| StatsPlugin (StatisticsViewer JAR) | 1.8 |
| PlaceableDecor | 1.1.0 |
| Final Station Map (Pl3xMap fork) | 26.3-555-finalstation-3 |

The source projects now share a folder layout, formatting rules, build tool and installation documentation. The source bundle includes the four projects, build tools and licenses. Generated files and private server data are excluded.

The map source now includes the default block-property fallback and redstone-power clamp already present in the deployed build. Its build compiles the patches and preserves upstream licenses. SpawnGuard's local source reload message color code was restored to match the deployed JAR. Rebuilt plugin classes retain the deployed behavior.

## Installation

Stop the server, back up plugin data, replace the existing JAR for each plugin being updated, then restart. Keep only one JAR per plugin. PlaceableDecor replaces the old PikminDecor JAR and retains its legacy data identifiers. Existing configuration is preserved; see each project's README for requirements and configuration.

The attached source ZIP contains build instructions. `SHA256SUMS.txt` covers the JARs and source ZIP.

## Validation

All four plugins compiled with Java 25 and passed a disposable Paper 26.3 startup, command and shutdown check. The map also passed a fresh resource extraction check and checks for the recovered block-property and redstone fixes. This check does not replace in-game multiplayer testing.

This is a draft release. Repository cleanup is staged locally and has not been committed or pushed. The attached source bundle corresponds to these rebuilt assets; the release tag currently targets `main`.
