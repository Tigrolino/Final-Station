scoreboard players set #winner murder 0
function finalstation:murder/end/begin
title @a[tag=murder.player] times 10 100 30
title @a[tag=murder.player] subtitle {"text":"Nobody survived the night","color":"gray","italic":true}
title @a[tag=murder.player] title {"text":"GAME OVER","color":"dark_red","bold":true}
execute as @a[tag=murder.player] at @s run playsound minecraft:block.bell.resonate master @s ~ ~ ~ 1 0.5
