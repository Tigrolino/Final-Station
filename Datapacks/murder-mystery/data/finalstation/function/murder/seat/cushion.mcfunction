$tag @e[type=minecraft:cushion,distance=..2,sort=nearest,limit=1] add murder.seat$(i)
$tag @e[type=minecraft:cushion,tag=murder.seat$(i)] add murder.cushion
$execute at @e[type=minecraft:cushion,tag=murder.seat$(i),limit=1] run tp @s ~ ~ ~ $(yaw) 0
$ride @s mount @e[type=minecraft:cushion,tag=murder.seat$(i),limit=1]
tag @s add murder.seated
