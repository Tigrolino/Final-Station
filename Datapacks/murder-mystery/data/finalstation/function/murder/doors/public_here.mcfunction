scoreboard players set #ray murder 99
execute unless entity @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] run function finalstation:murder/doors/register
execute as @e[type=minecraft:interaction,tag=murder.door,distance=..0.5,tag=murder.public] run return run function finalstation:murder/doors/unpublic_one
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.sealed
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] remove murder.decor
tag @e[type=minecraft:interaction,tag=murder.door,distance=..0.5] add murder.public
tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Door made public: ","color":"white"},{"text":"anyone can open it.","color":"green"}]
particle minecraft:dust{color:[0.2,1.0,0.2],scale:1.5} ~ ~1 ~ 0.3 0.6 0.3 0 30 force @s
playsound minecraft:block.wooden_door.open master @s ~ ~ ~ 1 1.2
