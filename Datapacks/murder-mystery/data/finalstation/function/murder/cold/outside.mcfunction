scoreboard players add @s murder.cold 1
title @s actionbar {"text":"It's cold outside. Go back inside!","color":"gray"}
# the cold makes them slow, slower after 15 s
execute if score @s murder.cold matches ..299 run effect give @s minecraft:slowness 2 0 true
execute if score @s murder.cold matches 300.. run effect give @s minecraft:slowness 2 1 true
# freezing damage, faster and faster: from 7 s every 2 s, from 15 s every second, from 23 s twice a second
scoreboard players operation #m murder = @s murder.cold
execute if score @s murder.cold matches 140..299 run scoreboard players operation #m murder %= #40 murder
execute if score @s murder.cold matches 300..459 run scoreboard players operation #m murder %= #20 murder
execute if score @s murder.cold matches 460.. run scoreboard players operation #m murder %= #10 murder
execute if score @s murder.cold matches 140.. if score #m murder matches 0 run function finalstation:murder/cold/bite
