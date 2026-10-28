# One murderer, one detective (never the murderer), everyone else a guest or an employee.
tag @a remove murder.innocent
tag @a remove murder.murderer
tag @a remove murder.guest
tag @a remove murder.employee
tag @a remove murder.detective
scoreboard players reset * murder.role
scoreboard players reset * murder.job
execute store result score #count murder if entity @a[tag=murder.player]
scoreboard players operation #start_count murder = #count murder
execute if score #count murder matches 1 as @a[tag=murder.player] run return run function finalstation:murder/roles/solo
tag @r[tag=murder.player] add murder.murderer
tag @r[tag=murder.player,tag=!murder.murderer] add murder.detective
execute as @a[tag=murder.player] run function finalstation:murder/roles/finish
