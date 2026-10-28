scoreboard players set @s seedling_lock 3
scoreboard players set @s seedling_stage 0
execute store result score @s seedling_stage run random value 1..1000
playsound minecraft:block.amethyst_block.chime player @s ~ ~ ~ 0.9 1.4
playsound minecraft:entity.experience_orb.pickup player @s ~ ~ ~ 0.65 1.1
particle minecraft:happy_villager ~ ~1 ~ 0.35 0.45 0.35 0.06 18 force @s
particle minecraft:cherry_leaves ~ ~1 ~ 0.25 0.35 0.25 0.02 10 force @s
execute if score @s seedling_stage matches 1..210 run function seedling:reward/red_bud
execute if score @s seedling_stage matches 211..360 run function seedling:reward/yellow_bud
execute if score @s seedling_stage matches 361..480 run function seedling:reward/blue_bud
execute if score @s seedling_stage matches 481..552 run function seedling:reward/purple_bud
execute if score @s seedling_stage matches 553..600 run function seedling:reward/white_bud
execute if score @s seedling_stage matches 601..705 run function seedling:reward/red_leaf
execute if score @s seedling_stage matches 706..780 run function seedling:reward/yellow_leaf
execute if score @s seedling_stage matches 781..840 run function seedling:reward/blue_leaf
execute if score @s seedling_stage matches 841..876 run function seedling:reward/purple_leaf
execute if score @s seedling_stage matches 877..900 run function seedling:reward/white_leaf
execute if score @s seedling_stage matches 901..935 run function seedling:reward/red_flower
execute if score @s seedling_stage matches 936..960 run function seedling:reward/yellow_flower
execute if score @s seedling_stage matches 961..980 run function seedling:reward/blue_flower
execute if score @s seedling_stage matches 981..992 run function seedling:reward/purple_flower
execute if score @s seedling_stage matches 993..1000 run function seedling:reward/white_flower
