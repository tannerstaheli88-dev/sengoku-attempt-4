execute as @e[type=minecraft:illusioner,nbt={PatrolLeader:1b}] run data merge entity @s {PatrolLeader:0b}
execute as @e[type=minecraft:illusioner] run function shioh:shinobi/modify
execute as @e[type=sengoku:shinobi_lord] run function shioh:shinobi/modify
tag @s add shioh.checked