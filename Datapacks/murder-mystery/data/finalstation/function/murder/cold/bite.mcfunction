# freezing damage; the last bite is not a real death but a game death (body, ghost)
execute store result score #hp murder run data get entity @s Health
scoreboard players set #cause murder 2
execute if score #hp murder matches ..2 run return run function finalstation:murder/knife/kill
# out_of_world gets through the immunity everyone has during the game (a custom damage type would need a server restart)
damage @s 2 minecraft:out_of_world
playsound minecraft:entity.player.hurt_freeze player @a ~ ~ ~ 1 1
