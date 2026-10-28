execute unless entity @a[tag=murder.player,tag=!murder.placed] run return 0
scoreboard players add #i murder 1
execute as @r[tag=murder.player,tag=!murder.placed] run function finalstation:murder/seat/place
function finalstation:murder/seat/next
