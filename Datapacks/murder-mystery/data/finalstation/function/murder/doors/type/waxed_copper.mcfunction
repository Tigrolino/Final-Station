$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=north,hinge=left] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=north,hinge=left,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=north,hinge=right] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=north,hinge=right,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=south,hinge=left] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=south,hinge=left,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=south,hinge=right] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=south,hinge=right,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=east,hinge=left] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=east,hinge=left,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=east,hinge=right] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=east,hinge=right,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=west,hinge=left] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=west,hinge=left,half=lower,open=$(open)]
$execute if block ~ ~ ~ minecraft:waxed_copper_door[facing=west,hinge=right] run setblock ~ ~ ~ minecraft:waxed_copper_door[facing=west,hinge=right,half=lower,open=$(open)]
$playsound minecraft:block.copper_door.$(act) block @a ~ ~1 ~ 1 1
