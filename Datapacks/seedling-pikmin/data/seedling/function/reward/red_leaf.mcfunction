give @s minecraft:flint[minecraft:custom_name={text:"Red Pikmin with leaf",color:"red",italic:false},minecraft:item_model="seedling:pikmin/red_leaf",minecraft:custom_data={seedling_pikmin:"red_leaf"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Red Pikmin with leaf","color":"red"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
