# as the player who clicked
# building-trick doors: they just do nothing
execute if entity @e[type=minecraft:interaction,tag=murder.door,tag=murder.this,tag=murder.decor] run return 0
execute if entity @e[type=minecraft:interaction,tag=murder.door,tag=murder.this,tag=murder.public] as @e[type=minecraft:interaction,tag=murder.door,tag=murder.this] at @s run return run function finalstation:murder/doors/use_public
execute if entity @e[type=minecraft:interaction,tag=murder.door,tag=murder.this,scores={murder.door=1..}] unless items entity @s weapon.* *[minecraft:custom_data~{murder_key:1b}] run return 0
execute if entity @e[type=minecraft:interaction,tag=murder.door,tag=murder.this,tag=murder.sealed] run return run function finalstation:murder/doors/denied
execute unless items entity @s weapon.* *[minecraft:custom_data~{murder_key:1b}] run return run function finalstation:murder/doors/denied
execute as @e[type=minecraft:interaction,tag=murder.door,tag=murder.this] at @s run function finalstation:murder/doors/use_key
