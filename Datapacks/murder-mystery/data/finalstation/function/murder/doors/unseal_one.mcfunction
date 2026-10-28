tag @s remove murder.sealed
tellraw @a[tag=murder.toolholder] ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"Door unsealed: ","color":"white"},{"text":"the Staff Key opens it.","color":"gold"}]
particle minecraft:dust{color:[1.0,0.8,0.1],scale:1.5} ~ ~1 ~ 0.3 0.6 0.3 0 30 force @a[tag=murder.toolholder]
