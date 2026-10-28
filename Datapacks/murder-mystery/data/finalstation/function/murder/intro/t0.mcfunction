effect give @a[tag=murder.player] minecraft:blindness 7 0 true
title @a[tag=murder.player] times 20 50 20
title @a[tag=murder.player] subtitle {"text":"The doors close behind you…","color":"gray","italic":true}
title @a[tag=murder.player] title {"text":""}
execute as @a[tag=murder.player] at @s run playsound minecraft:block.iron_door.close master @s ~ ~ ~ 1 0.5
execute as @a[tag=murder.player] at @s run playsound minecraft:ambient.cave master @s ~ ~ ~ 1 0.6
