# /function finalstation:murder/doors/decor  -  look at a door: switches it between locked and decoration (never opens, no locked message)
scoreboard players set #ray murder 0
tag @s add murder.toolholder
execute anchored eyes positioned ^ ^ ^ run function finalstation:murder/doors/ray {act:"decor"}
tag @s remove murder.toolholder
