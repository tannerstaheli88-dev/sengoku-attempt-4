
execute as @e[type=minecraft:drowned] run data merge entity @s {IsBaby:0}
execute as @e[type=drowned] unless entity @s[name="Kojin"] run attribute @s minecraft:generic.scale base set 0.8

tag @s add shioh.checked