give @s minecraft:flint[minecraft:custom_name={text:"Yellow Pikmin with flower",color:"yellow",italic:false},minecraft:item_model="seedling:pikmin/yellow_flower",minecraft:custom_data={seedling_pikmin:"yellow_flower"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Yellow Pikmin with flower","color":"yellow"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
