execute if predicate shioh:percentages/50percent run function shioh:skeleton/chest/golden_chestplate
execute if predicate shioh:percentages/50percent run function shioh:skeleton/chest/straw
execute if predicate shioh:percentages/50percent run function shioh:skeleton/chest/iron_chestplate
execute if predicate shioh:percentages/50percent run function shioh:skeleton/chest/leather_chestplate

data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_chest] {ArmorDropChances:[0.000F,0.000F,0.000F,0.000F]}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_chest] run tag @s add shioh_chest