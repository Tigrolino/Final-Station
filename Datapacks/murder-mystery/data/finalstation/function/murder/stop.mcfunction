# /function finalstation:murder/stop  -  ends the event and cleans everything up (players stay where they are)
execute as @a[tag=murder.gm_saved] run function finalstation:murder/gamemode/restore
function #finalstation:murder/stop
function finalstation:murder/end/cleanup
scoreboard players set #state murder 0
bossbar set finalstation:murder visible false
bossbar set finalstation:murder players
tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Murder mystery stopped.","color":"gray"}]
tag @a remove murder.player
tag @a remove murder.arrived
tag @a remove murder.placed
tag @a remove murder.host
tag @a remove murder.gm_saved
