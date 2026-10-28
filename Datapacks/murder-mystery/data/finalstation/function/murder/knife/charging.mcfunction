# the durability bar on the knife fills up while it recharges
execute store result storage finalstation:murder args.left int 1 run scoreboard players get @s murder.knife
scoreboard players operation #s murder = @s murder.knife
scoreboard players add #s murder 19
scoreboard players operation #s murder /= #20 murder
execute if items entity @s weapon.mainhand *[minecraft:custom_data~{murder_knife:1b}] run title @s actionbar [{"text":"🔪 ","color":"dark_red"},{"text":"Knife ready in ","color":"gray"},{"score":{"name":"#s","objective":"murder"},"color":"white"},{"text":"s","color":"gray"}]
execute store result score #full murder run scoreboard players get #knife_cooldown murder
scoreboard players operation #p murder = @s murder.knife
scoreboard players operation #p murder *= #1000 murder
scoreboard players operation #p murder /= #full murder
scoreboard players set #q murder 1000
scoreboard players operation #q murder -= #p murder
execute store result storage finalstation:murder args.frac float 0.001 run scoreboard players get #q murder
function finalstation:murder/knife/bar with storage finalstation:murder args
