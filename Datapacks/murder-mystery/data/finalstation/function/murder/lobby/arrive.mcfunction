# as one player: fade in at the lobby
tag @s add murder.arrived
function finalstation:murder/gamemode/adventure
effect give @s minecraft:blindness 2 0 true
function finalstation:murder/lobby/tp with storage finalstation:murder lobby
title @s times 20 60 30
title @s subtitle {"text":"The Final Station","color":"gray","italic":true}
title @s title [{"text":"MURDER MYSTERY","color":"dark_red","bold":true}]
playsound minecraft:block.bell.resonate master @s ~ ~ ~ 0.6 0.6
playsound minecraft:ambient.cave master @s ~ ~ ~ 1 0.8
bossbar set finalstation:murder players @a[tag=murder.player]
