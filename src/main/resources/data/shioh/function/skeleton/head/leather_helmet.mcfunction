execute if predicate shioh:percentages/5percent run data modify entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] ArmorItems[3] set value {id:"minecraft:leather_helmet",Count:1b}

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head