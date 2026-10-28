# as the door: open it (or close it again if it is open)
execute if score @s murder.door matches 1.. run return run function finalstation:murder/doors/close
playsound minecraft:block.vault.insert_item block @a ~ ~1 ~ 0.7 1.4
execute positioned ~ ~0.1 ~ align xyz run function finalstation:murder/doors/set {open:"true",act:"open"}
scoreboard players set @s murder.door 100
