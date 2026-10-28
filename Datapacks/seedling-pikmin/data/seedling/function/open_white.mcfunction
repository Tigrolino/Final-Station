advancement revoke @s only seedling:open_white_seedling
execute unless score @s seedling_lock matches 1.. run function seedling:open_white_once
