scoreboard players set @s seedling_stage 0
execute store result score @s seedling_stage run random value 1..1000
execute if score @s seedling_stage matches 1..200 run function seedling:reward/red_spooky
execute if score @s seedling_stage matches 201..400 run function seedling:reward/yellow_spooky
execute if score @s seedling_stage matches 401..600 run function seedling:reward/blue_spooky
execute if score @s seedling_stage matches 601..800 run function seedling:reward/purple_spooky
execute if score @s seedling_stage matches 801..1000 run function seedling:reward/white_spooky
