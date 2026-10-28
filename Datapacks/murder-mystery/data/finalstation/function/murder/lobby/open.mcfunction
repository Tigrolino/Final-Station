execute if score #state murder matches 1 as @a[tag=murder.player,tag=!murder.arrived] run function finalstation:murder/lobby/arrive
execute if score #state murder matches 1 run return 0
scoreboard players set #state murder 1
bossbar set finalstation:murder players @a[tag=murder.player]
bossbar set finalstation:murder visible true
bossbar set finalstation:murder value 0
execute as @a[tag=murder.player] run function finalstation:murder/lobby/arrive
function #finalstation:murder/lobby_open
