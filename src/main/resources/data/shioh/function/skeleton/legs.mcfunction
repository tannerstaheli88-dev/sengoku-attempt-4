execute if predicate shioh:percentages/50percent run function shioh:skeleton/legs/golden_leggings
execute if predicate shioh:percentages/50percent run function shioh:skeleton/legs/iron_leggings
execute if predicate shioh:percentages/50percent run function shioh:skeleton/legs/leather_leggings

data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_legs] {ArmorDropChances:[0.000F,0.000F,0.000F,0.000F]}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_legs] run tag @s add shioh_legs