give @s minecraft:flint[minecraft:custom_name={text:"Blue Pikmin with leaf",color:"aqua",italic:false},minecraft:item_model="seedling:pikmin/blue_leaf",minecraft:custom_data={seedling_pikmin:"blue_leaf"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Blue Pikmin with leaf","color":"aqua"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
