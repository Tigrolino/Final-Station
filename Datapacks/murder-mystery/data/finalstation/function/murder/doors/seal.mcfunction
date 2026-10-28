# /function finalstation:murder/doors/seal  -  look at a door: switches it between locked (Staff Key opens it) and sealed (nobody can open it)
scoreboard players set #ray murder 0
tag @s add murder.toolholder
execute anchored eyes positioned ^ ^ ^ run function finalstation:murder/doors/ray {act:"seal"}
tag @s remove murder.toolholder
