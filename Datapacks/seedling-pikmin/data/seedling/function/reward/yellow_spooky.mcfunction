give @s minecraft:flint[minecraft:custom_name={text:"Spooky Yellow Pikmin",color:"yellow",italic:false},minecraft:item_model="seedling:pikmin/yellow_spooky",minecraft:custom_data={seedling_pikmin:"yellow_spooky"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Spooky Yellow Pikmin","color":"yellow"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
