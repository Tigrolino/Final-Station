$execute in minecraft:murder_mystery run gamerule fire_spread_radius_around_player $(fire)
execute in minecraft:murder_mystery if data storage finalstation:murder old{monsters:1b} run gamerule spawn_monsters true
execute in minecraft:murder_mystery if data storage finalstation:murder old{pvp:0b} run gamerule pvp false
execute in minecraft:murder_mystery if data storage finalstation:murder old{regen:1b} run gamerule natural_health_regeneration true
