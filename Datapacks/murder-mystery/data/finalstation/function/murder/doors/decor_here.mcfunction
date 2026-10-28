scoreboard players set #ray murder 99
execute unless entity @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] run function finalstation:murder/doors/register
execute as @e[type=minecraft:interaction,tag=murder.door,distance=..0.5,tag=murder.decor] run return run function finalstation:murder/doors/undecor_one
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.sealed
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.public
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] add murder.decor
tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Door set as decoration: ","color":"white"},{"text":"it never opens and shows nothing.","color":"gray"}]
particle minecraft:dust{color:[0.8,0.8,0.8],scale:1.5} ~ ~1 ~ 0.3 0.6 0.3 0 30 force @s
