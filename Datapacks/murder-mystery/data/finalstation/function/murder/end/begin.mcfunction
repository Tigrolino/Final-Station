scoreboard players set #state murder 4
scoreboard players set #et murder 0
title @a[tag=murder.player] clear
execute as @a[tag=murder.player] run function finalstation:murder/cold/thaw
tellraw @a[tag=murder.player] ["",{"text":"━━━━━━━━━━━━━━━━━━━━","color":"dark_gray"}]
execute if score #winner murder matches 1 run tellraw @a[tag=murder.player] ["  ",{"text":"INNOCENTS WIN","color":"green","bold":true}]
execute if score #winner murder matches 2 run tellraw @a[tag=murder.player] ["  ",{"text":"THE MURDERER WINS","color":"dark_red","bold":true}]
execute if score #winner murder matches 0 run tellraw @a[tag=murder.player] ["  ",{"text":"GAME OVER","color":"dark_red","bold":true}]
execute if entity @a[tag=murder.murderer] run tellraw @a[tag=murder.player] ["",{"text":"  Murderer: ","color":"gray"},{"selector":"@a[tag=murder.murderer]","color":"red"}]
execute if entity @a[tag=murder.detective] run tellraw @a[tag=murder.player] ["",{"text":"  Detective: ","color":"gray"},{"selector":"@a[tag=murder.detective]","color":"blue"}]
execute if entity @a[tag=murder.player,tag=murder.innocent,tag=!murder.dead] run tellraw @a[tag=murder.player] ["",{"text":"  Survivors: ","color":"gray"},{"selector":"@a[tag=murder.player,tag=murder.innocent,tag=!murder.dead]","color":"white"}]
tellraw @a[tag=murder.player] ["",{"text":"━━━━━━━━━━━━━━━━━━━━","color":"dark_gray"}]
tellraw @a[tag=murder.player] {"text":"  Back to the lobby in 10 seconds…","color":"dark_gray","italic":true}
function #finalstation:murder/game_end
