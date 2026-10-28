# as the victim, at their position
# #cause: 1 = knife, 2 = cold
tag @s add murder.dead
function finalstation:murder/cold/thaw
execute if score #cause murder matches 1 run playsound minecraft:item.trident.hit player @a ~ ~ ~ 1 0.6
playsound minecraft:entity.player.death player @a ~ ~ ~ 1 0.8
execute if score #cause murder matches 1 run particle minecraft:block{block_state:"minecraft:redstone_block"} ~ ~1 ~ 0.3 0.5 0.3 0 60 normal
# the body: a mannequin with the player's skin, lying on the floor
summon minecraft:mannequin ~ ~ ~ {Tags:["murder.body","murder.newbody"],pose:"sleeping",immovable:1b,Invulnerable:1b,Silent:1b,hide_description:1b,PersistenceRequired:1b}
execute store result entity @e[type=minecraft:mannequin,tag=murder.newbody,limit=1] Rotation[0] float 1 run data get entity @s Rotation[0]
loot replace entity @e[type=minecraft:mannequin,tag=murder.newbody,limit=1] weapon.mainhand loot finalstation:murder/head
data modify entity @e[type=minecraft:mannequin,tag=murder.newbody,limit=1] profile set from entity @e[type=minecraft:mannequin,tag=murder.newbody,limit=1] equipment.mainhand.components."minecraft:profile"
item replace entity @e[type=minecraft:mannequin,tag=murder.newbody] weapon.mainhand with minecraft:air
tag @e[type=minecraft:mannequin,tag=murder.newbody] remove murder.newbody
# everything they carried falls next to the body (but the murderer's knife never does)
clear @s minecraft:iron_sword[minecraft:custom_data~{murder_knife:1b}]
execute if items entity @s hotbar.0 * run function finalstation:murder/knife/drop {slot:"hotbar.0"}
execute if items entity @s hotbar.1 * run function finalstation:murder/knife/drop {slot:"hotbar.1"}
execute if items entity @s hotbar.2 * run function finalstation:murder/knife/drop {slot:"hotbar.2"}
execute if items entity @s hotbar.3 * run function finalstation:murder/knife/drop {slot:"hotbar.3"}
execute if items entity @s hotbar.4 * run function finalstation:murder/knife/drop {slot:"hotbar.4"}
execute if items entity @s hotbar.5 * run function finalstation:murder/knife/drop {slot:"hotbar.5"}
execute if items entity @s hotbar.6 * run function finalstation:murder/knife/drop {slot:"hotbar.6"}
execute if items entity @s hotbar.7 * run function finalstation:murder/knife/drop {slot:"hotbar.7"}
execute if items entity @s hotbar.8 * run function finalstation:murder/knife/drop {slot:"hotbar.8"}
execute if items entity @s inventory.0 * run function finalstation:murder/knife/drop {slot:"inventory.0"}
execute if items entity @s inventory.1 * run function finalstation:murder/knife/drop {slot:"inventory.1"}
execute if items entity @s inventory.2 * run function finalstation:murder/knife/drop {slot:"inventory.2"}
execute if items entity @s inventory.3 * run function finalstation:murder/knife/drop {slot:"inventory.3"}
execute if items entity @s inventory.4 * run function finalstation:murder/knife/drop {slot:"inventory.4"}
execute if items entity @s inventory.5 * run function finalstation:murder/knife/drop {slot:"inventory.5"}
execute if items entity @s inventory.6 * run function finalstation:murder/knife/drop {slot:"inventory.6"}
execute if items entity @s inventory.7 * run function finalstation:murder/knife/drop {slot:"inventory.7"}
execute if items entity @s inventory.8 * run function finalstation:murder/knife/drop {slot:"inventory.8"}
execute if items entity @s inventory.9 * run function finalstation:murder/knife/drop {slot:"inventory.9"}
execute if items entity @s inventory.10 * run function finalstation:murder/knife/drop {slot:"inventory.10"}
execute if items entity @s inventory.11 * run function finalstation:murder/knife/drop {slot:"inventory.11"}
execute if items entity @s inventory.12 * run function finalstation:murder/knife/drop {slot:"inventory.12"}
execute if items entity @s inventory.13 * run function finalstation:murder/knife/drop {slot:"inventory.13"}
execute if items entity @s inventory.14 * run function finalstation:murder/knife/drop {slot:"inventory.14"}
execute if items entity @s inventory.15 * run function finalstation:murder/knife/drop {slot:"inventory.15"}
execute if items entity @s inventory.16 * run function finalstation:murder/knife/drop {slot:"inventory.16"}
execute if items entity @s inventory.17 * run function finalstation:murder/knife/drop {slot:"inventory.17"}
execute if items entity @s inventory.18 * run function finalstation:murder/knife/drop {slot:"inventory.18"}
execute if items entity @s inventory.19 * run function finalstation:murder/knife/drop {slot:"inventory.19"}
execute if items entity @s inventory.20 * run function finalstation:murder/knife/drop {slot:"inventory.20"}
execute if items entity @s inventory.21 * run function finalstation:murder/knife/drop {slot:"inventory.21"}
execute if items entity @s inventory.22 * run function finalstation:murder/knife/drop {slot:"inventory.22"}
execute if items entity @s inventory.23 * run function finalstation:murder/knife/drop {slot:"inventory.23"}
execute if items entity @s inventory.24 * run function finalstation:murder/knife/drop {slot:"inventory.24"}
execute if items entity @s inventory.25 * run function finalstation:murder/knife/drop {slot:"inventory.25"}
execute if items entity @s inventory.26 * run function finalstation:murder/knife/drop {slot:"inventory.26"}
execute if items entity @s weapon.offhand * run function finalstation:murder/knife/drop {slot:"weapon.offhand"}
execute if items entity @s armor.head * run function finalstation:murder/knife/drop {slot:"armor.head"}
execute if items entity @s armor.chest * run function finalstation:murder/knife/drop {slot:"armor.chest"}
execute if items entity @s armor.legs * run function finalstation:murder/knife/drop {slot:"armor.legs"}
execute if items entity @s armor.feet * run function finalstation:murder/knife/drop {slot:"armor.feet"}
# the player becomes a ghost
ride @s dismount
tag @s remove murder.seated
gamemode spectator @s
effect give @s minecraft:darkness 3 0 true
title @s times 5 60 20
execute if score #cause murder matches 1 run title @s subtitle {"text":"You were killed","color":"gray","italic":true}
execute if score #cause murder matches 2 run title @s subtitle {"text":"You froze to death","color":"aqua","italic":true}
title @s title {"text":"YOU DIED","color":"dark_red","bold":true}
tellraw @s ["",{"text":"[","color":"gray"},{"text":"🔪","color":"dark_red"},{"text":"] ","color":"gray"},{"text":"You are now a ghost. Watch, but do not tell!","color":"gray","italic":true}]
function #finalstation:murder/death
