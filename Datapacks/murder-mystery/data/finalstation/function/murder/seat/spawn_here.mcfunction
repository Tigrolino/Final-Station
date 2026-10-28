$tp @s ~ ~ ~ $(yaw) 0
# the seat is an invisible display entity; if its chunk is still loading, seat/keep mounts the player a moment later
$summon minecraft:item_display ~ ~ ~ {Tags:["murder.seat","murder.seat$(i)"],Rotation:[$(yaw)f,0f]}
$ride @s mount @e[type=minecraft:item_display,tag=murder.seat$(i),limit=1]
tag @s add murder.seated
playsound minecraft:block.wool.place master @s ~ ~ ~ 1 0.8
