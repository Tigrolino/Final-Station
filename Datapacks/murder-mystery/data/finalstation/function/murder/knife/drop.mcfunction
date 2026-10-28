summon minecraft:item ~ ~0.3 ~ {Tags:["murder.drop","murder.newdrop"],Item:{id:"minecraft:stone",count:1},PickupDelay:20}
$item replace entity @e[type=minecraft:item,tag=murder.newdrop,limit=1] contents from entity @s $(slot)
$item replace entity @s $(slot) with minecraft:air
tag @e[type=minecraft:item,tag=murder.newdrop] remove murder.newdrop
