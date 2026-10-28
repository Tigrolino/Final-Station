# as one player: turn the tags into scores
scoreboard players set @s murder.role 1
scoreboard players set @s[tag=murder.murderer] murder.role 2
tag @s[tag=!murder.murderer] add murder.innocent
execute store result score @s murder.job run random value 1..2
scoreboard players set @s[tag=murder.detective] murder.job 3
tag @s[scores={murder.job=1}] add murder.guest
tag @s[scores={murder.job=2}] add murder.employee
