give @s minecraft:flint[minecraft:custom_name={text:"Red Pikmin with flower",color:"red",italic:false},minecraft:item_model="seedling:pikmin/red_flower",minecraft:custom_data={seedling_pikmin:"red_flower"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Red Pikmin with flower","color":"red"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
