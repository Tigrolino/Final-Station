# no cushion left: spread over the standing spots (several players can share one)
scoreboard players operation #s murder = #i murder
scoreboard players remove #s murder 11
scoreboard players operation #s murder %= #4 murder
execute store result storage finalstation:murder args.i int 1 run scoreboard players get #s murder
function finalstation:murder/seat/stand_pick with storage finalstation:murder args
