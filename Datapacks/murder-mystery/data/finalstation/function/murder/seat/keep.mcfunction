# Seated players who get up (sneak) are put straight back until the host releases them.
tag @a remove murder.riding
execute as @a[tag=murder.seated] on vehicle on passengers run tag @s add murder.riding
execute as @a[tag=murder.seated,tag=!murder.riding] run function finalstation:murder/seat/remount
execute as @a[tag=murder.standing] at @s run function finalstation:murder/seat/hold
