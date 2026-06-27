tag @s add shioh.checked
execute as @e[type=minecraft:piglin] run data merge entity @s {IsBaby:0}
execute as @e[type=piglin] run attribute @s minecraft:generic.scale base set 1.2

execute if predicate shioh:percentages/50percent run function shioh:piglin/datsueba

