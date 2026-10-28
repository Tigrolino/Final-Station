tag @s add murder.placed
scoreboard players operation @s murder.seat = #i murder
execute if score #i murder matches ..10 run return run function finalstation:murder/seat/sit
function finalstation:murder/seat/stand
