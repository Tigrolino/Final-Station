give @s minecraft:flint[minecraft:custom_name={text:"Spooky Purple Pikmin",color:"dark_purple",italic:false},minecraft:item_model="seedling:pikmin/purple_spooky",minecraft:custom_data={seedling_pikmin:"purple_spooky"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Spooky Purple Pikmin","color":"dark_purple"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
