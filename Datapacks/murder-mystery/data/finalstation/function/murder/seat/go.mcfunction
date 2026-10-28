# as the player, at the cushion block
$execute in minecraft:murder_mystery positioned $(x) $(y) $(z) align xyz positioned ~0.5 ~ ~0.5 run function finalstation:murder/seat/height
execute store result storage finalstation:murder seat.h double 0.0625 run scoreboard players get #h murder
execute store result storage finalstation:murder seat.i int 1 run scoreboard players get @s murder.seat
function finalstation:murder/seat/spawn with storage finalstation:murder seat
