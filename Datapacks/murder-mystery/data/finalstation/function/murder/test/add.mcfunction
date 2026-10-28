# /function finalstation:murder/test/add {name:"PlayerName"}  -  adds one player to the lobby while testing
$execute unless entity $(name) run return run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"$(name) is not online.","color":"red"}]
$tag $(name) add murder.player
$execute if score #state murder matches 1 as $(name) run function finalstation:murder/lobby/arrive
$execute unless score #state murder matches 1 run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"$(name) will join when the lobby opens.","color":"gray"}]
