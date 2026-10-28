# /function finalstation:murder/lobby  -  brings EVERYONE online to the lobby
execute if score #state murder matches 2.. run return run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"The event is already running. Use /function finalstation:murder/stop first.","color":"red"}]
tag @a add murder.player
function finalstation:murder/lobby/open
