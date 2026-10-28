execute if entity @s[tag=murder.gm_survival] run gamemode survival @s
execute if entity @s[tag=murder.gm_creative] run gamemode creative @s
execute if entity @s[tag=murder.gm_adventure] run gamemode adventure @s
execute if entity @s[tag=murder.gm_spectator] run gamemode spectator @s
tag @s remove murder.gm_survival
tag @s remove murder.gm_creative
tag @s remove murder.gm_adventure
tag @s remove murder.gm_spectator
tag @s remove murder.gm_saved
