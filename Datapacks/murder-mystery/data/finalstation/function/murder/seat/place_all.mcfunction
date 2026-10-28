# The host gets the seat at the head of the table; everyone else in a random order: cushions 1-10, then the standing spots.
tag @a remove murder.placed
execute as @a[tag=murder.host,tag=murder.player] run function finalstation:murder/seat/place_host
scoreboard players set #i murder 0
function finalstation:murder/seat/next
