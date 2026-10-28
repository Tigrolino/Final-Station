# at the bottom centre of the lower door block
summon minecraft:interaction ~ ~-0.03 ~ {width:1.06f,height:2.06f,response:1b,Tags:["murder.door"]}
# doors start out closed
execute if block ~ ~ ~ #minecraft:doors[open=true] run function finalstation:murder/doors/set {open:"false",act:"close"}
