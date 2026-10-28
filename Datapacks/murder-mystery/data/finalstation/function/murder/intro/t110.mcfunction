effect clear @a[tag=murder.player] minecraft:blindness
title @a[tag=murder.player] times 15 70 20
title @a[tag=murder.player] subtitle {"text":"One of you is not who they seem…","color":"gray","italic":true}
title @a[tag=murder.player] title [{"text":"MURDER MYSTERY","color":"dark_red","bold":true}]
execute as @a[tag=murder.player] at @s run playsound minecraft:entity.lightning_bolt.thunder master @s ~ ~ ~ 0.5 0.7
execute as @a[tag=murder.player] at @s run playsound minecraft:block.bell.resonate master @s ~ ~ ~ 0.8 0.5
execute as @a[tag=murder.player] at @s run particle minecraft:large_smoke ~ ~1 ~ 3 0.5 3 0.005 30 normal @s
