execute as @a[tag=murder.player,tag=!murder.dead,gamemode=!spectator] at @s if dimension minecraft:murder_mystery run function finalstation:murder/cold/check
execute as @a[scores={murder.cold=1..}] at @s unless dimension minecraft:murder_mystery run function finalstation:murder/cold/thaw
