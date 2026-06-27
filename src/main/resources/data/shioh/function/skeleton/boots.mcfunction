execute if predicate shioh:percentages/50percent run function shioh:skeleton/boots/golden_boots
execute if predicate shioh:percentages/50percent run function shioh:skeleton/boots/iron_boots
execute if predicate shioh:percentages/50percent run function shioh:skeleton/boots/leather_boots

data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_boots] {ArmorDropChances:[0.000F,0.000F,0.000F,0.000F]}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_boots] run tag @s add shioh_boots