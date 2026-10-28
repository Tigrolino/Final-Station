# /function finalstation:murder/doors/public  -  look at a door: switches it between locked (Staff Key opens it) and public (anyone can open it)
scoreboard players set #ray murder 0
tag @s add murder.toolholder
execute anchored eyes positioned ^ ^ ^ run function finalstation:murder/doors/ray {act:"public"}
tag @s remove murder.toolholder
