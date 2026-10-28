give @s minecraft:flint[minecraft:custom_name={text:"Blue Pikmin with bud",color:"aqua",italic:false},minecraft:item_model="seedling:pikmin/blue_bud",minecraft:custom_data={seedling_pikmin:"blue_bud"}] 1
tellraw @s [{"text":"Discovered ","color":"gray"},{"text":"Blue Pikmin with bud","color":"aqua"},{"text":"!","color":"gray"}]
playsound minecraft:entity.item.pickup player @s ~ ~ ~ 0.8 1.2
