# at game start, for everyone taking part
effect give @s minecraft:resistance infinite 4 true
effect give @s minecraft:saturation infinite 0 true
effect give @s minecraft:instant_health 1 10 true
tag @s remove murder.dead
scoreboard players reset @s murder.cold
scoreboard players set @s murder.knife 0
execute if entity @s[tag=murder.murderer] run item replace entity @s hotbar.1 with minecraft:iron_sword[minecraft:custom_name={"text":"Knife","color":"dark_red","italic":false},minecraft:lore=[{"text":"One stab is enough.","color":"gray","italic":true}],minecraft:custom_data={murder_knife:1b},minecraft:weapon={item_damage_per_attack:0},minecraft:tooltip_display={hidden_components:["minecraft:attribute_modifiers"]}]
execute if entity @s[tag=murder.murderer] run playsound minecraft:item.armor.equip_iron master @s ~ ~ ~ 1 0.6
