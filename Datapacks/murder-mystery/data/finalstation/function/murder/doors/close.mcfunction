# never shut the door on someone standing in it: try again next tick
execute positioned ~-0.5 ~ ~-0.5 if entity @a[dx=0,dy=1,dz=0] run return run scoreboard players set @s murder.door 1
scoreboard players reset @s murder.door
execute positioned ~ ~0.1 ~ align xyz run function finalstation:murder/doors/set {open:"false",act:"close"}
