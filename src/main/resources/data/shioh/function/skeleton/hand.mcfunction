execute store result score $server_difficulty shioh.diff run difficulty
item replace entity @s weapon.mainhand with stone_sword
item replace entity @s weapon.offhand with shield
data merge entity @s {DeathLootTable:"shioh:skeleton_with_sword"}

execute if predicate shioh:percentages/10percent run data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {HandItems:[{id:"minecraft:stone_sword",Count:1},{}]}
execute if predicate shioh:percentages/10percent run data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {HandItems:[{id:"minecraft:wooden_sword",Count:1},{}]}
execute if predicate shioh:percentages/10percent run data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {HandItems:[{id:"minecraft:stone_axe",Count:1},{}]}
execute if predicate shioh:percentages/10percent run data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {HandItems:[{id:"minecraft:golden_axe",Count:1},{}]}
execute if predicate shioh:percentages/5percent run data merge entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] {HandItems:[{id:"minecraft:bow",Count:1},{}]}
execute if predicate shioh:percentages/10percent run item replace entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] weapon.mainhand with sengoku:stone_naginata 1
execute if predicate shioh:percentages/10percent run item replace entity @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] weapon.mainhand with sengoku:stone_yari 1

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_hand] run tag @s add shioh_hand