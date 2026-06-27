tag @s add shioh.checked
execute as @e[type=minecraft:strider] run data merge entity @s {Age:0}
execute if predicate shioh:percentages/50percent run function shioh:strider/katawaguruma
