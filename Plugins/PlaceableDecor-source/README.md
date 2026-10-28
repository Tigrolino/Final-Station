# PlaceableDecor

Places collectible items as stationary decorations using Minecraft's cushion placement rules. The default config includes 20 Pikmin variants. The [hat example](examples/config-with-hats.yml) adds the 37 named hats from the resource pack.

## Install

Requires Paper 26.3, Java 25 and the resource pack containing the configured models. Keep SpawnGuard installed for spawn protection.

Copy `PlaceableDecor-1.1.0.jar` into the server's `plugins` folder and restart. Remove any old PikminDecor JAR first. On the first run, the plugin imports an existing `plugins/PikminDecor/config.yml` if its new config does not exist. Existing placed items keep working.

## Use

- Right-click a surface with a configured item to place it. The model stays upright and faces the player who placed it.
- Shelves, doors, chests and buttons keep their normal interactions. Sneak + right-click to place on an interactive block instead.
- Punch a decoration to collect it. Removing its support also breaks it and drops the original item.
- Survival placement uses one item. Creative placement uses none; creative breaking produces no drop.
- Everyone can place and collect decorations outside spawn. SpawnGuard's configured operator bypass applies inside spawn.

## Commands

| Command | Purpose |
| --- | --- |
| `/placeabledecor reload` | Reload variant definitions |
| `/placeabledecor status` | Show loaded decorations and server tick statistics |

`/pikmindecor` remains an alias. Administration requires `finalstation.decor.admin`, granted to operators by default. The old `finalstation.pikmin.admin` permission remains supported.

## Configuration

Edit `plugins/PlaceableDecor/config.yml`. A variant matches the material and item model, with optional exact plain-text `custom-name` and top-level custom-data string checks:

```yaml
variants:
  hat_frog:
    material: CARVED_PUMPKIN
    item-model: minecraft:carved_pumpkin
    custom-name: "Frog"
    scale: 1.0
    offset-y: 0.5
    yaw-offset: 0.0
    hitbox-width: 0.9
    hitbox-height: 1.0
```

Omit both `custom-data-key` and `custom-data-value` when matching without custom data. Use a distinct material/model/name combination for each variant. Unlisted items are ignored.

Distances are in blocks; yaw is in degrees. With display transform `NONE`, a model whose lowest point is Y=0 normally needs `offset-y` equal to half its scale. Hitbox width must be positive and at most one block. Values under `defaults` apply unless overridden per variant. Invalid reloads leave the previous definitions active. Rendering changes affect new placements.

## Build

From this project folder with Java 25 and Maven:

```sh
mvn package
```

The JAR is written to `target/`. Native cushion access is isolated in `NativeCushions`; check compatibility when changing Minecraft versions.

The integration harness in `tests/` is for a disposable server only. It creates test objects and shuts the server down. Visual appearance and sound still need an in-game check with the resource pack.
