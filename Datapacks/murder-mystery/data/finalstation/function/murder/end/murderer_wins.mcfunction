scoreboard players set #winner murder 2
function finalstation:murder/end/begin
title @a[tag=murder.player] times 10 120 30
title @a[tag=murder.player] subtitle [{"text":"The murderer was ","color":"gray"},{"selector":"@a[tag=murder.murderer]","color":"red"}]
title @a[tag=murder.player] title {"text":"THE MURDERER WINS","color":"dark_red","bold":true}
execute as @a[tag=murder.player] at @s run playsound minecraft:entity.wither.death master @s ~ ~ ~ 0.6 0.7
execute as @a[tag=murder.player] at @s run playsound minecraft:block.bell.resonate master @s ~ ~ ~ 1 0.5
execute at @a[tag=murder.player,tag=murder.murderer] run summon minecraft:firework_rocket ~ ~2 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"burst",colors:[I;11141120,1644825]}]}}}}
