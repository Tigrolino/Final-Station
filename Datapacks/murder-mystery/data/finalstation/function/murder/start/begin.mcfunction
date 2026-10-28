execute if score #state murder matches 2.. run return run tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"The event is already running. Use /function finalstation:murder/stop first.","color":"red"}]
tag @a remove murder.host
tag @s[type=player] add murder.host
scoreboard players set #state murder 2
scoreboard players set #released murder 0
scoreboard players set #t murder 0
# the boss bar is only for the lobby
bossbar set finalstation:murder visible false
function finalstation:murder/forceload with storage finalstation:murder table
execute as @a[tag=murder.player] run function finalstation:murder/freeze
execute as @a[tag=murder.player] run function finalstation:murder/gamemode/adventure
function finalstation:murder/sky/storm
