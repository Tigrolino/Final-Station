# /function finalstation:murder/test/lobby  -  brings only YOU to the lobby
execute if score #state murder matches 2.. run return run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"The event is already running. Use /function finalstation:murder/stop first.","color":"red"}]
tag @s add murder.player
function finalstation:murder/lobby/open
