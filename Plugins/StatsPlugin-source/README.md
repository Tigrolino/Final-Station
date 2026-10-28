# StatsPlugin

Player statistics menus, leaderboards and AFK, recording and live status tags. The server plugin is named `StatsPlugin`; the JAR is named `StatisticsViewer`.

Original plugin by StarMaster, maintained here for The Final Station.

## Requirements

Paper 26.3 and Java 25. PlaceholderAPI is optional and enables the status placeholders below.

## Commands

| Command | Purpose |
| --- | --- |
| `/stats` | Open the statistics menu |
| `/stats reload` | Reload settings; requires `statsplugin.reload` |
| `/afk` | Toggle AFK status |
| `/recording` | Toggle the recording tag; alias `/rec` |
| `/live` | Toggle the live tag; alias `/stream` |

`/statistics` and `/playerstats` are aliases for `/stats`. The reload permission defaults to operators.

## Configuration

Settings are stored under `plugins/StatsPlugin/`. Configure the AFK timeout and messages, recording/live messages, cache intervals, leaderboard page size and main-menu buttons in `config.yml`. Accumulated AFK time is stored in `afkdata.yml`.

Menu buttons use stable internal statistic identifiers. Renaming a button does not change its leaderboard identifier or title. Recording and live toggles reset when the player leaves; AFK totals persist.

## TAB placeholders

| Placeholder | Active value |
| --- | --- |
| `%statsplugin_afk%` | Gray `[AFK]` tag |
| `%statsplugin_rec%` | Red `[REC]` tag |
| `%statsplugin_live%` | Purple `[LIVE]` tag |

These values include a trailing space. Use the `_tag` variants to omit it. Add the placeholders to `tabprefix` or `tagprefix` in TAB's `groups.yml`, including `_DEFAULT_` and any group overrides:

```yaml
_DEFAULT_:
  tabprefix: "%statsplugin_afk%%statsplugin_rec%%statsplugin_live%%luckperms-prefix%"
  tagprefix: "%statsplugin_afk%%statsplugin_rec%%statsplugin_live%%luckperms-prefix%"
```

## Build

```sh
mvn package
```

Run from this project folder with Java 25 and Maven. The output is `target/StatisticsViewer-1.8.jar`. PlaceholderAPI and JetBrains annotations are compile-time dependencies; neither is bundled in the JAR.
