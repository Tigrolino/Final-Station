# warming up again, three times as fast as freezing
scoreboard players remove @s murder.cold 3
execute if score @s murder.cold matches ..0 run scoreboard players reset @s murder.cold
execute if score @s murder.cold matches 1.. run return 0
function finalstation:murder/cold/thaw
# fully warm again: heal up slowly
effect give @s minecraft:regeneration 12 1 true
