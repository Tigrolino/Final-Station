# /function finalstation:murder/test/knife  -  gives you the knife (and makes you the murderer) for testing
tag @s add murder.murderer
item replace entity @s hotbar.1 with minecraft:iron_sword[minecraft:custom_name={"text":"Knife","color":"dark_red","italic":false},minecraft:lore=[{"text":"One stab is enough.","color":"gray","italic":true}],minecraft:custom_data={murder_knife:1b},minecraft:weapon={item_damage_per_attack:0},minecraft:tooltip_display={hidden_components:["minecraft:attribute_modifiers"]}]
scoreboard players set @s murder.knife 0
