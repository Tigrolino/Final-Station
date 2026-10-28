give @s minecraft:flint[minecraft:custom_name={text:"Spooky Blue Pikmin",color:"aqua",italic:false},minecraft:item_model="seedling:pikmin/blue_spooky",minecraft:custom_data={seedling_pikmin:"blue_spooky"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Spooky Blue Pikmin","color":"aqua"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
