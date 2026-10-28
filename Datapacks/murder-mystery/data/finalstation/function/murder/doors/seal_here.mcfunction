scoreboard players set #ray murder 99
execute unless entity @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] run function finalstation:murder/doors/register
execute as @e[type=minecraft:interaction,tag=murder.door,distance=..0.5,tag=murder.sealed] run return run function finalstation:murder/doors/unseal_one
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.public
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.decor
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] add murder.sealed
tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Door sealed: ","color":"white"},{"text":"nobody can open it.","color":"red"}]
particle minecraft:dust{color:[1.0,0.1,0.1],scale:1.5} ~ ~1 ~ 0.3 0.6 0.3 0 30 force @s
playsound minecraft:block.iron_door.close master @s ~ ~ ~ 1 0.6
