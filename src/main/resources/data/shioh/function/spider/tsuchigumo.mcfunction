execute as @e[type=cave_spider] run attribute @s minecraft:generic.scale base set 3
execute as @e[type=minecraft:cave_spider,tag=!hp_set] run attribute @s minecraft:generic.max_health base set 30
execute as @e[type=minecraft:cave_spider,tag=!hp_set] run data merge entity @s {Health:30.0f}
tag @e[type=minecraft:cave_spider,tag=!hp_set] add hp_set
tag @s add shioh.checked