# New players get the guide once, the first time they are seen in the Overworld ("world").
execute as @a[tag=!finalstation.guide_given] at @s if dimension minecraft:overworld run function finalstation:guide/give
# Players who already got a guide have their old copy swapped for the current one.
execute as @a[tag=finalstation.guide_given] unless score @s finalstation.guide matches 3.. run function finalstation:guide/update
