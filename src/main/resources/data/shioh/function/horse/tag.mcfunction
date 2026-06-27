execute as @e[type=zombie_horse] run data merge entity @s {Tame:1b}
execute as @e[type=skeleton_horse] run data merge entity @s {Tame:1b}
execute as @e[type=skeleton_horse] run attribute @s minecraft:generic.scale base set 1.2

tag @s add shioh.checked