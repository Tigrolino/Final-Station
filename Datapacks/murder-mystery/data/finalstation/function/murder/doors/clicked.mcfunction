# as the door, at its position: who clicked it?
tag @s add murder.this
execute on target run function finalstation:murder/doors/try
tag @s remove murder.this
data remove entity @s interaction
