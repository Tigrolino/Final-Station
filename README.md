# The Final Station

Skripts, datapacks and the resource pack for **The Final Station** Minecraft SMP.

---

## Folder layout

```
Final Station/
├── Datapacks/        → one folder per datapack (goes in world/datapacks/)
├── Resourcepack/     → the server resource pack
├── Skripts/          → Skript files (goes in plugins/Skript/scripts/)
├── Releases/         → zipped builds (not uploaded to git, use GitHub Releases)
├── README.md
└── STYLE.md          → how files in this repo are named and formatted
```

## Requirements

| What | Used for |
| --- | --- |
| Minecraft **1.21.10** | Datapacks support pack formats 88 – 121 |
| [Skript](https://github.com/SkriptLang/Skript) | everything in `Skripts/` |
| [LuckPerms](https://luckperms.net) | `ranks.sk` (builder / elytra groups) |
| [Multiverse-Core](https://github.com/Multiverse/Multiverse-Core) | `ban.sk`, the creative / flat worlds |
| [Geyser](https://geysermc.org) + Floodgate | Bedrock players in `whitelist.sk` |

## Datapacks

| Folder | What it does | Made by |
| --- | --- | --- |
| `final-station-crafting` | All our custom crafting recipes (see below) | us, via [TheDestruc7i0n's Crafting Generator](https://crafting.thedestruc7i0n.ca) |
| `custom-paintings` | Adds our own paintings (textures are in the resource pack) | us |
| `painting-picker` | Pick any painting (incl. ours) with a stonecutter | [Vanilla Tweaks](https://vanillatweaks.net), edited |
| `smelted-leather` | Rotten flesh → leather in a furnace, smoker or campfire | us |
| `all-achievements` | "Completionist" advancement for getting every vanilla advancement | us |

<details>
<summary>Custom recipes in <code>final-station-crafting</code></summary>

| Recipe | Result |
| --- | --- |
| Dirt + Hanging Roots | Rooted Dirt |
| 2 Sand + 2 Dirt | 4 Sand |
| Grass Block | Dirt |
| Cobblestone + Bone Meal | Calcite |
| Cobblestone + Basalt | 2 Tuff |
| 2 Ferns | Large Fern |
| 2 Short Grass | Tall Grass |
| Fern + Bone Meal | 2 Ferns |
| Short Grass + Bone Meal | 2 Short Grass |
| Vine / Twisting Vines / Weeping Vines + Bone Meal | 2 of the same vine |
| Poppy + Coal | Wither Rose |
| Skeleton Skull surrounded by Gunpowder | Creeper Head |
| Skeleton Skull surrounded by Gold Ingots | Piglin Head |
| Stone surrounded by Bones | Skeleton Skull |
| Skeleton Skull surrounded by Rotten Flesh | Zombie Head |
| Conduit surrounded by 8 Nautilus Shells | 2 Conduits |

</details>

## Skripts

| File | What it does | Who it's for |
| --- | --- | --- |
| `ban.sk` | Soft ban: sends a specific player to the `test` world on join | Banned players |
| `height.sk` | `/height` – play at your real-life height | Everyone (`/height`), admins (the rest) |
| `ranks.sk` | `/builder`, `/elytra` (+ `/un...`) – LuckPerms group shortcuts | Admins only |
| `restart-broadcast.sk` | `/serverrestart` countdown and `/broadcast <gold/red>` | Admins only |
| `server-start.sk` | `/start`, `/startnow`, `/reset` – the big server-start event | Admins only |
| `spells.sk` | Experimental items | Admins only |
| `welcome.sk` | Join/quit messages, first-join welcome, `/day` counter | Everyone |
| `whitelist.sk` | `/wladd`, `/wlapply`, ... – batch whitelist for Java + Bedrock | Admins only |
| `worlds.sk` | `/creative`, `/survival`, `/flat`, `/gmc`, `/spawn`, ... | Builders |

Every Skript file starts with a header listing its commands and permissions.

## Resource pack

Contains the staff hats (`hat_1` – `hat_36`), the staffs (bread / forest / void), sonion rings and
the custom paintings.

## Credits

- Tigrolino
- The3dGamerz

## License

MIT – see [LICENSE](LICENSE).
