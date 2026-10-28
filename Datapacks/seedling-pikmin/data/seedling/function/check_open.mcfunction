scoreboard players set @s seedling_flint_use 0
# The Seedling is the only flint item configured as usable/consumable.
execute if entity @s[nbt={SelectedItem:{id:"minecraft:flint",components:{"minecraft:custom_data":{seedling_box:1b}}}}] run function seedling:open
