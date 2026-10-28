# as one player: their role card in chat (/trigger role shows it again)
tellraw @s ["",{"text":"━━━━━━━━━━━━━━━━━━━━","color":"dark_gray"}]
execute if entity @s[tag=murder.murderer] run tellraw @s ["",{"text":"  ☠ ","color":"dark_red"},{"text":"MURDERER","color":"dark_red","bold":true}]
execute if entity @s[tag=murder.innocent] run tellraw @s ["",{"text":"  ✔ ","color":"green"},{"text":"INNOCENT","color":"green","bold":true}]
execute if entity @s[tag=murder.guest] run tellraw @s ["",{"text":"  Job: ","color":"gray"},{"text":"Guest","color":"gold"}]
execute if entity @s[tag=murder.employee] run tellraw @s ["",{"text":"  Job: ","color":"gray"},{"text":"Employee","color":"aqua"}]
execute if entity @s[tag=murder.detective] run tellraw @s ["",{"text":"  Job: ","color":"gray"},{"text":"Detective","color":"blue"}]
execute if entity @s[tag=murder.murderer] run tellraw @s {"text":"  Leave no witnesses. Keep your role secret.","color":"gray","italic":true}
execute if entity @s[tag=murder.innocent,tag=!murder.detective] run tellraw @s {"text":"  Survive, and help find the killer.","color":"gray","italic":true}
execute if entity @s[tag=murder.detective] run tellraw @s {"text":"  Find the killer before it is too late.","color":"gray","italic":true}
execute if entity @s[tag=murder.employee] run tellraw @s ["",{"text":"  You carry the ","color":"gray","italic":true},{"text":"Staff Key","color":"gold","italic":true},{"text":". It opens the locked doors.","color":"gray","italic":true}]
tellraw @s ["",{"text":"━━━━━━━━━━━━━━━━━━━━","color":"dark_gray"}]
tellraw @s {"text":"  /trigger role shows this again","color":"dark_gray"}
playsound minecraft:ui.toast.in master @s ~ ~ ~ 1 1
