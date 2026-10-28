execute if entity @e[type=minecraft:item,distance=..1,nbt={Item:{id:"minecraft:white_dye"}},limit=1] run function seedling:cauldron_recipe/white
execute if entity @e[type=minecraft:item,distance=..1,nbt={Item:{id:"minecraft:orange_dye"}},limit=1] unless entity @e[type=minecraft:item,tag=seedling_recipe_checked,distance=..1] run function seedling:cauldron_recipe/gold
execute if entity @e[type=minecraft:item,distance=..1,nbt={Item:{id:"minecraft:yellow_dye"}},limit=1] unless entity @e[type=minecraft:item,tag=seedling_recipe_checked,distance=..1] run function seedling:cauldron_recipe/normal
