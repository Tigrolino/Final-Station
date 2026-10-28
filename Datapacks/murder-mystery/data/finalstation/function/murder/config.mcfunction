# ==========================================================
# All places used by the event. Change them here, then /reload.
# ==========================================================
# Lobby (waiting area)
data modify storage finalstation:murder lobby set value {x:939,y:-59,z:1446}
# The 10 cushions at the table
# filled in this order, so the first players sit opposite each other (yaw 0 = facing south, 180 = facing north)
data modify storage finalstation:murder seats set value [{x:95,y:-60,z:383,yaw:0},{x:95,y:-60,z:387,yaw:180},{x:97,y:-60,z:383,yaw:0},{x:97,y:-60,z:387,yaw:180},{x:93,y:-60,z:383,yaw:0},{x:93,y:-60,z:387,yaw:180},{x:99,y:-60,z:383,yaw:0},{x:99,y:-60,z:387,yaw:180},{x:91,y:-60,z:383,yaw:0},{x:91,y:-60,z:387,yaw:180}]
# The host (whoever runs start) always sits here, at the head of the table, facing west
data modify storage finalstation:murder host_seat set value {x:101,y:-59,z:385,yaw:90}
# Kept loaded while the event runs (corners of the dining room), so the seats are always there
data modify storage finalstation:murder table set value {x1:85,z1:376,x2:105,z2:394}
# How long the knife needs after a kill before it can kill again (ticks, 20 = 1 second)
scoreboard players set #knife_cooldown murder 600
# Where players stand when all cushions are taken (several can share a spot)
data modify storage finalstation:murder standing set value [{x:103,y:-59,z:382,yaw:60,pitch:0},{x:103,y:-59,z:388,yaw:120,pitch:0},{x:88,y:-59,z:388,yaw:-120,pitch:0},{x:88,y:-59,z:382,yaw:-60,pitch:0}]
