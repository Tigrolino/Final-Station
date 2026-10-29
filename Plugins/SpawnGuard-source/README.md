# SpawnGuard

Protects The Final Station's spawn region and controls plant growth and mob block damage.

## Requirements

Paper 26.3 and Java 25.

## Behavior

- Vines do not spread naturally. Bone meal grows them down, sideways or upward when space allows.
- Endermen cannot move blocks. Creeper explosions can hurt entities but do not damage blocks or decorations.
- Leash knots resist non-player damage and leads do not snap from distance. Player removal and the Leash Fences datapack remain supported.
- The configured spawn region blocks building and changes to decorations. Operators can bypass player restrictions when enabled.
- Fluids, pistons, fire, explosions and mobs cannot damage the region.
- Doors and containers remain usable. Optional settings prevent PvP, hunger loss, anvil wear and selected mob spawns.

## Commands and configuration

Settings are stored in `plugins/SpawnGuard/config.yml`. `/spawnguard reload` reloads the config. It requires `spawnguard.reload`, granted to operators by default.

`blocked-growth` controls natural plant spread. `no-block-damage-mobs` applies in every world. Settings under `region` control its world, corners, operator bypass and protection rules.

## Build

```sh
mvn package
```

Run from this project folder with Java 25 and Maven. The output is `target/SpawnGuard-1.4.jar`.
