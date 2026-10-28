scoreboard players set #out murder 1
execute if predicate finalstation:murder/area/indoors run scoreboard players set #out murder 0
execute if predicate finalstation:murder/area/patio run scoreboard players set #out murder 0
execute if predicate finalstation:murder/area/open_1 run scoreboard players set #out murder 1
execute if predicate finalstation:murder/area/open_2 run scoreboard players set #out murder 1
execute if score #out murder matches 1 run function finalstation:murder/cold/outside
execute if score #out murder matches 0 if score @s murder.cold matches 1.. run function finalstation:murder/cold/inside
