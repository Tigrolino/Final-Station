$execute if block ~ ~ ~ #minecraft:doors[half=upper] positioned ~ ~-1 ~ align xyz positioned ~0.5 ~ ~0.5 run return run function finalstation:murder/doors/$(act)_here
$execute if block ~ ~ ~ #minecraft:doors[half=lower] align xyz positioned ~0.5 ~ ~0.5 run return run function finalstation:murder/doors/$(act)_here
scoreboard players add #ray murder 1
$execute if score #ray murder matches ..60 positioned ^ ^ ^0.1 run return run function finalstation:murder/doors/ray {act:"$(act)"}
execute if score #ray murder matches 61 run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Look at a door (within 6 blocks) and run it again.","color":"red"}]
