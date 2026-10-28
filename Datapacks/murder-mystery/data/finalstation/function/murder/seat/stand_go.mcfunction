$execute in minecraft:murder_mystery run tp @s $(x) $(y) $(z) $(yaw) $(pitch)
# a marker on the spot keeps the player there until everyone stands up
execute at @s run summon minecraft:marker ~ ~ ~ {Tags:["murder.anchor","murder.newanchor"]}
scoreboard players operation @e[type=minecraft:marker,tag=murder.newanchor] murder.seat = @s murder.seat
tag @e[type=minecraft:marker,tag=murder.newanchor] remove murder.newanchor
tag @s add murder.standing
