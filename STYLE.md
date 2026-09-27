# Style guide

The rules every file in this repo follows. The `.editorconfig` file makes most editors
(VS Code, IntelliJ, Notepad++ with plugin) do this automatically.

## All files

- UTF-8, **LF** line endings (`.gitattributes` enforces this on commit), newline at the end of the file
- No trailing spaces, never more than 2 empty lines in a row

## Names

| Thing | Style | Example |
| --- | --- | --- |
| Top-level folders | Capitalized | `Datapacks/`, `Skripts/` |
| Skript files | `kebab-case.sk`, named after what they do | `server-start.sk` |
| Datapack folders | `kebab-case` | `final-station-crafting` |
| Anything inside a pack (namespaces, recipes, textures...) | `snake_case` (Minecraft requires lowercase) | `skeleton_skull.json` |
| Our own namespace | `finalstation` | `finalstation:heads/zombie_head` |
| Our permissions | `finalstation.<feature>` | `finalstation.flat` |

## Skript (`.sk`)

- Indent with **4 spaces**, never tabs
- Every file starts with a header:

```
# ==========================================================
# file-name.sk
#
# One or two lines about what the file does.
#
# Commands:
#   /command <args>   what it does
#
# Permissions:
#   finalstation.something
#
# Requires: other plugins it needs (if any)
# ==========================================================
```

- Every command / event group gets a section block, with 2 empty lines before it and 1 after:

```
# ==========================================================
# /COMMAND OR SECTION NAME
# ==========================================================
```

- Chat prefix for server messages: `&7[&6🚂&r&7]&r `
- Keep things that belong together in one file (e.g. all LuckPerms rank commands in `ranks.sk`)
  instead of making a new tiny file for every command.

## JSON (`.json`, `pack.mcmeta`)

- Indent with **2 spaces**
- Our own datapacks use `min_format` / `max_format` in `pack.mcmeta` and a description starting with
  `Final Station - `
- Crafting recipes go in `final-station-crafting/data/finalstation/recipe/<group>/<result_item>.json`
  where `<group>` is `blocks`, `plants`, `heads` or `misc`
- New small datapacks should be merged into an existing one instead of added as a new folder
