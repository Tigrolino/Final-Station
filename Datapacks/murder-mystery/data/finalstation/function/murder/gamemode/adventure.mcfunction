# remember the game mode once, then switch to adventure
execute unless entity @s[tag=murder.gm_saved] if entity @s[gamemode=survival] run tag @s add murder.gm_survival
execute unless entity @s[tag=murder.gm_saved] if entity @s[gamemode=creative] run tag @s add murder.gm_creative
execute unless entity @s[tag=murder.gm_saved] if entity @s[gamemode=adventure] run tag @s add murder.gm_adventure
execute unless entity @s[tag=murder.gm_saved] if entity @s[gamemode=spectator] run tag @s add murder.gm_spectator
tag @s add murder.gm_saved
gamemode adventure @s
