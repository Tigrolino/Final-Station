# night and a thunderstorm in the event world for the whole game
execute if score #storm murder matches 1 run return 0
scoreboard players set #storm murder 1
# lightning must never set the house on fire: no fire spread while the game runs (old value restored at stop)
execute in minecraft:murder_mystery store result storage finalstation:murder old.fire int 1 run gamerule fire_spread_radius_around_player
execute in minecraft:murder_mystery store result storage finalstation:murder old.monsters byte 1 run gamerule spawn_monsters
execute in minecraft:murder_mystery run gamerule fire_spread_radius_around_player 0
execute in minecraft:murder_mystery run gamerule spawn_monsters false
# hits must register for the knife (everyone is immune to normal damage, see knife/arm)
execute in minecraft:murder_mystery store result storage finalstation:murder old.pvp byte 1 run gamerule pvp
execute in minecraft:murder_mystery run gamerule pvp true
# the cold must be able to kill: no natural healing while the game runs (warming up inside heals instead)
execute in minecraft:murder_mystery store result storage finalstation:murder old.regen byte 1 run gamerule natural_health_regeneration
execute in minecraft:murder_mystery run gamerule natural_health_regeneration false
function finalstation:murder/sky/hold
