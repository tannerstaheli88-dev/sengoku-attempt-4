execute as @e[type=sengoku:warlord,tag=!shioh.checked] run function shioh:kensei/modify
execute as @e[type=vindicator,nbt={HandItems:[{id:"minecraft:iron_axe",count:1},{}]}] run function shioh:kensei/samurai
execute as @e[type=minecraft:pillager,tag=!shioh.checked] if data entity @s {ArmorItems:[{},{},{},{id:"minecraft:white_banner"}]} run item replace entity @s armor.head with air
execute as @e[type=minecraft:vindicator,tag=!shioh.checked] if data entity @s {ArmorItems:[{},{},{},{id:"minecraft:white_banner"}]} run item replace entity @s armor.head with air
execute as @e[type=minecraft:evoker,tag=!shioh.checked] if data entity @s {ArmorItems:[{},{},{},{id:"minecraft:white_banner"}]} run item replace entity @s armor.head with air
tag @s add shioh.checked