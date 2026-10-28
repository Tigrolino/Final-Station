# role as the big title, job underneath; stays up for 7 seconds
title @s times 0 140 30
execute if entity @s[tag=murder.guest] run title @s subtitle {"text":"Guest","color":"gold"}
execute if entity @s[tag=murder.employee] run title @s subtitle {"text":"Employee","color":"aqua"}
execute if entity @s[tag=murder.detective] run title @s subtitle {"text":"Detective","color":"blue"}
execute if entity @s[tag=murder.detective] run playsound minecraft:item.spyglass.use master @s ~ ~ ~ 1 0.8
execute if entity @s[tag=murder.murderer] run return run function finalstation:murder/roles/reveal_murderer
title @s title {"text":"INNOCENT","color":"green","bold":true}
playsound minecraft:block.beacon.activate master @s ~ ~ ~ 1 1.2
playsound minecraft:block.amethyst_block.resonate master @s ~ ~ ~ 1 1
