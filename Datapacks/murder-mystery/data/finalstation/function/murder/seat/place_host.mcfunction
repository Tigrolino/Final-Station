tag @s add murder.placed
scoreboard players set @s murder.seat 0
data modify storage finalstation:murder seat set from storage finalstation:murder host_seat
function finalstation:murder/seat/go with storage finalstation:murder seat
