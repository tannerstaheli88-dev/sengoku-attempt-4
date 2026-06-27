## Give nearby skeletons a chainmail helmet with a red Trim.
## Each block below targets the nearest skeleton without the tag `shioh_head`,
## gives it a chainmail helmet with a different red trim pattern, then tags it
## so subsequent lines apply to other skeletons.

## Assign a trimmed chainmail helmet by directly modifying the entity's ArmorItems[3]
## (helmet slot). Using `data modify` avoids parser issues with `item replace ... with <item> <nbt>`.

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:coast",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:stripe_down",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:border",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:bracer",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:chevron",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head

execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run data modify entity @s ArmorItems[3] set value {id:"minecraft:chainmail_helmet",Count:1b,tag:{Trim:{Pattern:"minecraft:gradient",Material:"minecraft:redstone"}}}
execute as @e[type=minecraft:skeleton,limit=1,sort=nearest,tag=!shioh_head] run tag @s add shioh_head