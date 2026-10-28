tag @s add seedling_recipe_checked
execute as @e[type=minecraft:item,distance=..1,nbt={Item:{id:"minecraft:orange_dye"}},limit=1] run kill @s
execute as the3dgamerz run function seedling:cauldron_recipe/reward_gold
kill @s
