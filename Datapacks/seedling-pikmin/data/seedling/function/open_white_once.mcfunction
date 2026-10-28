scoreboard players set @s seedling_lock 3
scoreboard players set @s seedling_roll 0
execute store result score @s seedling_roll run random value 1..1000
playsound minecraft:block.amethyst_block.chime player @s ~ ~ ~ 0.9 1.4
playsound minecraft:entity.experience_orb.pickup player @s ~ ~ ~ 0.65 1.1
particle minecraft:happy_villager ~ ~1 ~ 0.35 0.45 0.35 0.06 18 force @s
particle minecraft:cherry_leaves ~ ~1 ~ 0.25 0.35 0.25 0.02 10 force @s
execute if score @s seedling_roll matches 1..50 run function seedling:roll_spooky
execute if score @s seedling_roll matches 51..1000 run function seedling:roll_normal
