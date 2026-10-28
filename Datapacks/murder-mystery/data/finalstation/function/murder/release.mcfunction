scoreboard players set #released murder 1
# /function finalstation:murder/release  -  everyone may stand up and walk
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
execute as @a[tag=murder.player] run function finalstation:murder/unfreeze
execute as @a[tag=murder.player] at @s run playsound minecraft:block.wooden_door.open master @s ~ ~ ~ 1 0.8
tellraw @a[tag=murder.player] ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"You may leave the table.","color":"gray","italic":true}]
function #finalstation:murder/release
