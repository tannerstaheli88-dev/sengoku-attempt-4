execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_hand] run function shioh:skeleton/hand
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run function shioh:skeleton/head
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_chest] run function shioh:skeleton/chest
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_legs] run function shioh:skeleton/legs
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_boots] run function shioh:skeleton/boots
execute as @e[type=skeleton,tag=!skeleton] run data merge entity @s {Health:10.0f,LifeTicks:400,Tags:["skeleton"],Attributes:[{Name:"minecraft:generic.max_health",Base:10},{Name:generic.attack_damage,Base:0}]}
tag @s add shioh.checked