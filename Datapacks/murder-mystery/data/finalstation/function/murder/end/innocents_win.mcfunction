scoreboard players set #winner murder 1
function finalstation:murder/end/begin
title @a[tag=murder.player] times 10 120 30
title @a[tag=murder.player] subtitle [{"text":"The murderer was ","color":"gray"},{"selector":"@a[tag=murder.murderer]","color":"red"}]
title @a[tag=murder.player] title {"text":"INNOCENTS WIN","color":"green","bold":true}
execute as @a[tag=murder.player] at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
execute as @a[tag=murder.player] at @s run playsound minecraft:block.bell.use master @s ~ ~ ~ 1 1.2
# fireworks over the survivors
execute at @a[tag=murder.player,tag=murder.innocent,tag=!murder.dead] run summon minecraft:firework_rocket ~ ~2 ~ {LifeTime:20,FireworksItem:{id:"minecraft:firework_rocket",count:1,components:{"minecraft:fireworks":{explosions:[{shape:"large_ball",colors:[I;6618980,16777215],has_twinkle:true}]}}}}
