# the murderer is dead: everyone else wins (also when testing alone as the murderer)
execute if entity @a[tag=murder.player,tag=murder.murderer] unless entity @a[tag=murder.player,tag=murder.murderer,tag=!murder.dead] run return run function finalstation:murder/end/innocents_win
# every innocent is dead: the murderer wins
execute if entity @a[tag=murder.player,tag=murder.innocent] unless entity @a[tag=murder.player,tag=murder.innocent,tag=!murder.dead] if entity @a[tag=murder.player,tag=murder.murderer,tag=!murder.dead] run return run function finalstation:murder/end/murderer_wins
# nobody is left alive (e.g. testing alone as an innocent and freezing)
execute unless entity @a[tag=murder.player,tag=!murder.dead] run return run function finalstation:murder/end/nobody
