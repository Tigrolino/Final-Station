# Runs once per player per new book version: removes old guides from their inventory and,
# if they had one, gives them the current version instead. A guide kept in a chest is not touched.
scoreboard players set @s finalstation.guide 3
execute store result score #old finalstation.guide run clear @s minecraft:written_book[minecraft:item_model="finalstation:guide_book"]
execute store result score #older finalstation.guide run clear @s minecraft:written_book[minecraft:custom_data~{finalstation_guide:1}]
scoreboard players operation #old finalstation.guide += #older finalstation.guide
execute if score #old finalstation.guide matches 1.. run loot give @s loot finalstation:guide
execute if score #old finalstation.guide matches 1.. run playsound minecraft:item.book.page_turn player @s ~ ~ ~ 1 1
