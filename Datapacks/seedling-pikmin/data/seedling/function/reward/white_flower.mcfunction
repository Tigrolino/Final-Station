give @s minecraft:flint[minecraft:custom_name={text:"White Pikmin with flower",color:"white",italic:false},minecraft:item_model="seedling:pikmin/white_flower",minecraft:custom_data={seedling_pikmin:"white_flower"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"White Pikmin with flower","color":"white"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
