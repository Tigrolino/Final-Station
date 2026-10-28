scoreboard players remove @a[scores={seedling_lock=1..}] seedling_lock 1
function seedling:cauldron_recipe/tick

# Enable player triggers and route requests to the3dgamerz
scoreboard players enable @a give_seed
scoreboard players enable @a give_white_seed
scoreboard players enable @a give_gold_seed
execute as @a[scores={give_seed=1..}] run function seedling:trigger/give_seed
execute as @a[scores={give_white_seed=1..}] run function seedling:trigger/give_white_seed
execute as @a[scores={give_gold_seed=1..}] run function seedling:trigger/give_gold_seed
