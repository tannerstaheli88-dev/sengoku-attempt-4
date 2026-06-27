scoreboard objectives remove shioh.diff
schedule clear #shioh:1sec
execute as @e[type=skeleton,tag=shioh.check,nbt={HandItems:[{id:"minecraft:stone_sword",count:1},{}]}] at @s run tp @s ~ -128 ~
execute as @e[type=wither_skeleton,tag=shioh.check,nbt={HandItems:[{id:"minecraft:bow",count:1},{}]}] at @s run tp @s ~ -128 ~
execute as @e[type=zombie,tag=shioh.check,nbt={HandItems:[{id:"minecraft:stone_sword",count:1},{}]}] at @s run tp @s ~ -128 ~
tag @e[type=#skeletons,tag=shioh.check] remove shioh.check