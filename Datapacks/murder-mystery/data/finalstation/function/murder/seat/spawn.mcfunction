# A real cushion (the vanilla cushion entity) near the spot? Sit the player on it.
$execute in minecraft:murder_mystery positioned $(x) $(y) $(z) align xyz positioned ~0.5 ~ ~0.5 if entity @e[type=minecraft:cushion,distance=..2] run return run function finalstation:murder/seat/cushion {yaw:$(yaw),i:$(i)}
# Otherwise: an invisible seat on top of whatever block is there
$execute in minecraft:murder_mystery positioned $(x) $(y) $(z) align xyz positioned ~0.5 ~$(h) ~0.5 run function finalstation:murder/seat/spawn_here {yaw:$(yaw),i:$(i)}
