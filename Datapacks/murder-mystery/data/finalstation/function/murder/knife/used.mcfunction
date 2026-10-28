# as the murderer: start the cooldown
scoreboard players operation @s murder.knife = #knife_cooldown murder
item modify entity @s hotbar.1 {type:"minecraft:set_damage",damage:0.0}
playsound minecraft:entity.player.attack.crit master @s ~ ~ ~ 1 0.7
