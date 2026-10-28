# the murderer always has the knife: if it was dropped it comes straight back
execute as @a[tag=murder.murderer,tag=!murder.dead] unless items entity @s container.* *[minecraft:custom_data~{murder_knife:1b}] run function finalstation:murder/knife/return
# cooldown
scoreboard players remove @a[scores={murder.knife=1..}] murder.knife 1
execute as @a[scores={murder.knife=1..}] run function finalstation:murder/knife/charging
execute as @a[tag=murder.murderer,scores={murder.knife=0}] run function finalstation:murder/knife/ready
