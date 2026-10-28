# Murder mystery commands (ops)

| Command | What it does |
| --- | --- |
| `/function finalstation:murder/lobby` | Brings everyone online to the lobby (939 -59 1446 in `murder_mystery`) |
| `/function finalstation:murder/start` | Seats everyone at the table and hands out roles and jobs |
| `/function finalstation:murder/release` | Lets everyone stand up from the table (happens by itself once the roles are out) |
| `/function finalstation:murder/stop` | Ends the event and cleans everything up |
| `/function finalstation:murder/test/lobby` | Lobby, but only for you |
| `/function finalstation:murder/test/add {name:"Player"}` | Adds one player to your test |
| `/function finalstation:murder/test/start` | Start, but only for you and the players you added |
| `/function finalstation:murder/doors/seal` | Look at a door: switches it between locked (Staff Key opens it) and sealed (nobody can open it) |
| `/function finalstation:murder/doors/decor` | Look at a door: switches it between locked and decoration (never opens, shows nothing) |
| `/function finalstation:murder/doors/public` | Look at a door: switches it between locked (Staff Key opens it) and public (anyone can open it) |
| `/function finalstation:murder/doors/show` | Marks the doors near you: gold = Staff Key, green = public, red = sealed |
| `/function finalstation:murder/test/key` | Gives you a Staff Key for testing |
| `/function finalstation:murder/test/knife` | Makes you the murderer and gives you the knife, for testing |
| `/trigger role` | (players) shows their role card again |

While the game runs it is midnight with a thunderstorm in `murder_mystery` (no fire spread, no monsters), and everyone taking part is in adventure mode; stop puts it all back.
The murderer has a knife in slot 2: one stab kills (only once everyone has left the table), then it recharges for 30 seconds (`#knife_cooldown` in config). The dead drop their items, leave a body and become spectators. Everyone else is immune to damage.
In `murder_mystery` players (not in creative) cannot use any block (trapdoors, pots, buttons...) and paintings cannot be broken.
While the game runs, anyone outside the house freezes (slowness, then damage that gets faster until they die); the house areas are boxes in `data/finalstation/predicate/murder/area/`.
When the murderer dies, everyone else gets an INNOCENTS WIN screen; when every innocent is dead, THE MURDERER WINS. After 10 seconds all players go back to the lobby for the next round (hook: `game_end`). The murderer's knife is never dropped.
Every door in `murder_mystery` is locked (found automatically when someone comes near it). Employees get a Staff Key.
Places (lobby, cushions, standing spots) are in `data/finalstation/function/murder/config.mcfunction`.
Roles are in the scores `murder.role` (1 innocent, 2 murderer) and `murder.job` (1 guest, 2 employee, 3 detective),
and in tags like `murder.murderer` and `murder.detective`. Function tags in `data/finalstation/tags/function/murder/`
(`lobby_open`, `game_start`, `release`, `death`, `game_end`, `stop`) are hooks for adding more later.
