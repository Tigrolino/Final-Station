# Runs once per player, ever. The tag is stored with the player, so they never get a second copy.
tag @s add finalstation.guide_given
scoreboard players set @s finalstation.guide 3
loot give @s loot finalstation:guide
playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1
tellraw @s [{"text":"[","color":"gray"},{"text":"🚂","color":"gold"},{"text":"] ","color":"gray"},{"text":"You received ","color":"white"},{"text":"The Final Station: A Traveller's Guide","color":"gold"},{"text":". It explains everything special about the server. Keep it safe, there is only one!","color":"white"}]
