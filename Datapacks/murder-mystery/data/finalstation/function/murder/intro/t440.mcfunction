execute as @a[tag=murder.player] run function finalstation:murder/roles/card
execute as @a[tag=murder.employee] run function finalstation:murder/doors/give_key
execute as @a[tag=murder.player] run function finalstation:murder/knife/arm
scoreboard players enable @a[tag=murder.player] role
scoreboard players set #state murder 3
function #finalstation:murder/game_start
# roles are out: everyone stands up by themselves
function finalstation:murder/release
