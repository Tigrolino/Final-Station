# everyone (the dead too) goes back to the lobby for the next round
function finalstation:murder/end/cleanup
execute as @a[tag=murder.player] run gamemode adventure @s
tag @a remove murder.arrived
tag @a remove murder.placed
tag @a remove murder.host
scoreboard players set #state murder 0
function finalstation:murder/lobby/open
