# How high the cushion is, in 1/16 block (the player sits on top of it).
# The coordinates may point at the cushion block itself or at the air just above it.
function finalstation:murder/seat/height_of
execute unless block ~ ~ ~ #minecraft:air run return 0
execute positioned ~ ~-1 ~ run function finalstation:murder/seat/height_of
scoreboard players remove #h murder 16
execute if score #h murder matches ..-16 run scoreboard players set #h murder 0
