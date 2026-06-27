execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/leather_helmet
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/golden_helmet
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/straw_hat
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/sando_hat
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/iron_helmet
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/golden_helmet_menpo
execute if predicate shioh:percentages/10percent run function shioh:skeleton/head/iron_helmet_menpo


data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {ArmorDropChances:[0.000F,0.000F,0.000F,0.000F]}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head