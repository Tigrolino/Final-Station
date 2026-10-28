# every second while waiting
execute store result score #count murder if entity @a[tag=murder.player]
bossbar set finalstation:murder name [{"text":"Murder Mystery","color":"dark_red","bold":true},{"text":"  ·  ","color":"dark_gray"},{"text":"Waiting for guests ","color":"gray"},{"text":"(","color":"dark_gray"},{"score":{"name":"#count","objective":"murder"},"color":"white"},{"text":")","color":"dark_gray"}]
title @a[tag=murder.player] actionbar {"text":"The host will begin shortly…","color":"gray","italic":true}
# a distant clock now and then
execute store result score #r murder run random value 1..8
execute if score #r murder matches 1 as @a[tag=murder.player] at @s run playsound minecraft:block.note_block.basedrum ambient @s ~ ~ ~ 0.4 0.5
