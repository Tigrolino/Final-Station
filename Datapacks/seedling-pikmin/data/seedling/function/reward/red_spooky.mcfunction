give @s minecraft:flint[minecraft:custom_name={text:"Spooky Red Pikmin",color:"red",italic:false},minecraft:item_model="seedling:pikmin/red_spooky",minecraft:custom_data={seedling_pikmin:"red_spooky"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Spooky Red Pikmin","color":"red"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
