# runs as the player who was hit by the knife
advancement revoke @s only finalstation:murder/knife_hit
execute if entity @s[tag=murder.dead] run return 0
execute unless score #state murder matches 3 run return 0
# nobody dies at the dinner table: the knife only works after the host lets everyone stand up
execute unless score #released murder matches 1 run return 0
# was the knife ready?
tag @s add murder.victim
scoreboard players set #ok murder 0
execute on attacker if entity @s[tag=murder.murderer] unless score @s murder.knife matches 1.. run scoreboard players set #ok murder 1
execute if score #ok murder matches 1 on attacker run function finalstation:murder/knife/used
scoreboard players set #cause murder 1
execute if score #ok murder matches 1 at @s run function finalstation:murder/knife/kill
tag @s remove murder.victim
