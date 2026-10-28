# no jumping during the intro. (Walking is stopped by the seats / standing spots instead of a speed change,
# because a speed change zooms the camera in a lot.)
attribute @s minecraft:movement_speed modifier remove finalstation:murder_freeze
attribute @s minecraft:jump_strength modifier add finalstation:murder_freeze -1 add_multiplied_total
