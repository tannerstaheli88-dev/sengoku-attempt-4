execute if predicate shioh:percentages/5percent run data modify entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_chest] ArmorItems[2] set value {id:"minecraft:leather_chestplate",Count:1b}

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_chest] run tag @s add shioh_chest