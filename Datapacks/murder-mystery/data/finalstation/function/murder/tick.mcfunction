# doors are locked all the time, event or not
function finalstation:murder/doors/tick
execute if score #state murder matches 0 run return 0
scoreboard players add #tick murder 1
scoreboard players operation #sec murder = #tick murder
scoreboard players operation #sec murder %= #20 murder
execute if score #state murder matches 1 if score #sec murder matches 0 run function finalstation:murder/lobby/tick
execute if score #state murder matches 2 run function finalstation:murder/intro/tick
execute if score #state murder matches 2.. run function finalstation:murder/seat/keep
execute if score #state murder matches 2.. if score #sec murder matches 0 run function finalstation:murder/sky/hold
execute if score #state murder matches 3 run function finalstation:murder/knife/tick
execute if score #state murder matches 3 run function finalstation:murder/cold/tick
execute if score #state murder matches 3 run function finalstation:murder/end/check
execute if score #state murder matches 4 run function finalstation:murder/end/tick
execute as @a[scores={role=1..}] run function finalstation:murder/roles/again
