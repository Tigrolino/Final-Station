# players on a standing spot are put back on it if they walk off
scoreboard players operation #want murder = @s murder.seat
execute as @e[type=minecraft:marker,tag=murder.anchor] if score @s murder.seat = #want murder run tag @s add murder.target
execute unless entity @e[type=minecraft:marker,tag=murder.target,distance=..0.3] at @e[type=minecraft:marker,tag=murder.target,limit=1] run tp @s ~ ~ ~
tag @e[type=minecraft:marker,tag=murder.target] remove murder.target
