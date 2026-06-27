tag @s add shioh.checked
execute as @e[type=minecraft:zombified_piglin] run data merge entity @s {IsBaby:0}
execute as @e[type=zombified_piglin] run data modify entity @s HandItems[0] set value {id:"sengoku:stone_tetsubo",Count:1}
