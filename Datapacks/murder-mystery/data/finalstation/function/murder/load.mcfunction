# ==========================================================
# Murder mystery: objectives, the boss bar and the settings.
# ==========================================================
scoreboard objectives add murder dummy
scoreboard objectives add murder.role dummy
scoreboard objectives add murder.job dummy
scoreboard objectives add murder.seat dummy
scoreboard objectives add role trigger
scoreboard objectives add murder.door dummy
scoreboard objectives add murder.knife dummy
scoreboard objectives add murder.cold dummy
execute unless score #state murder matches 0.. run scoreboard players set #state murder 0
bossbar add finalstation:murder ""
bossbar set finalstation:murder color red
bossbar set finalstation:murder style notched_10
function finalstation:murder/config
scoreboard players set #4 murder 4
scoreboard players set #20 murder 20
scoreboard players set #1000 murder 1000
scoreboard players set #40 murder 40
scoreboard players set #10 murder 10
