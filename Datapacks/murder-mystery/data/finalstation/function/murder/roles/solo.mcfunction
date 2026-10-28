# Testing alone: random role that still follows the rules
execute store result score #r murder run random value 1..3
execute if score #r murder matches 1 run tag @s add murder.murderer
execute if score #r murder matches 2 run tag @s add murder.detective
function finalstation:murder/roles/finish
