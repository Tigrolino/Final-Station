give @s minecraft:flint[minecraft:custom_name={text:"Spooky White Pikmin",color:"white",italic:false},minecraft:item_model="seedling:pikmin/white_spooky",minecraft:custom_data={seedling_pikmin:"white_spooky"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Spooky White Pikmin","color":"white"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
