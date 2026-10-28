# everything a game leaves behind (used by stop and by the return to the lobby)
clear @a[tag=murder.player] minecraft:trial_key[minecraft:custom_data~{murder_key:1b}]
clear @a[tag=murder.player] minecraft:iron_sword[minecraft:custom_data~{murder_knife:1b}]
effect clear @a[tag=murder.player] minecraft:resistance
effect clear @a[tag=murder.player] minecraft:saturation
kill @e[type=minecraft:mannequin,tag=murder.body]
execute as @a[tag=murder.player] run function finalstation:murder/cold/thaw
scoreboard players reset * murder.cold
scoreboard players reset * murder.knife
kill @e[type=minecraft:item,tag=murder.drop]
kill @e[type=minecraft:item,predicate=finalstation:murder/knife_item]
advancement revoke @a only finalstation:murder/knife_hit
tag @a remove murder.dead
tag @a remove murder.seated
execute as @a[tag=murder.player] run ride @s dismount
kill @e[type=minecraft:item_display,tag=murder.seat]
kill @e[type=minecraft:marker,tag=murder.anchor]
tag @a remove murder.standing
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat0
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat1
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat2
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat3
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat4
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat5
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat6
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat7
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat8
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat9
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat10
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.seat
tag @e[type=minecraft:cushion,tag=murder.cushion] remove murder.cushion
function finalstation:murder/unforceload with storage finalstation:murder table
execute as @a[tag=murder.player] run function finalstation:murder/unfreeze
effect clear @a[tag=murder.player] minecraft:blindness
effect clear @a[tag=murder.player] minecraft:darkness
title @a[tag=murder.player] clear
scoreboard players reset @a[tag=murder.player] role
tag @a remove murder.innocent
tag @a remove murder.murderer
tag @a remove murder.guest
tag @a remove murder.employee
tag @a remove murder.detective
scoreboard players reset * murder.role
scoreboard players reset * murder.job
scoreboard players reset * murder.seat
execute if score #storm murder matches 1 run function finalstation:murder/sky/clear
