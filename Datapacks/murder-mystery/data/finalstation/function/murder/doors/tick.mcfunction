execute as @e[type=minecraft:interaction,tag=murder.door] if data entity @s interaction at @s run function finalstation:murder/doors/clicked
execute as @e[type=minecraft:interaction,tag=murder.door] if data entity @s attack run data remove entity @s attack
execute as @e[type=minecraft:interaction,tag=murder.door,scores={murder.door=1..}] at @s run function finalstation:murder/doors/countdown
# look for new doors around players in the event world, 4 times a second
scoreboard players add #dt murder 1
execute if score #dt murder matches 5.. run scoreboard players set #dt murder 0
execute if score #dt murder matches 0 as @a[gamemode=!spectator] at @s if dimension minecraft:murder_mystery align xyz run function finalstation:murder/doors/scan_near
# In the event world players cannot use any block at all (trapdoors, pots, flower pots, buttons...):
# their block reach is 0. Doors still work because their lock is an entity. Creative players are not affected.
execute if score #dt murder matches 0 as @a[tag=!murder.noblocks,gamemode=!creative,gamemode=!spectator] at @s if dimension minecraft:murder_mystery run function finalstation:murder/doors/hands_off
execute if score #dt murder matches 0 as @a[tag=murder.noblocks] at @s unless dimension minecraft:murder_mystery run function finalstation:murder/doors/hands_on
execute if score #dt murder matches 0 as @a[tag=murder.noblocks,gamemode=creative] run function finalstation:murder/doors/hands_on
