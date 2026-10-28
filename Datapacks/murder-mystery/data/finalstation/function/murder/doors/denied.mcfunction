title @s actionbar [{"text":"🔒 ","color":"gray"},{"text":"This door is locked","color":"red"}]
execute at @e[type=minecraft:interaction,tag=murder.door,tag=murder.this] run playsound minecraft:block.chest.locked block @a ~ ~1 ~ 0.8 1
