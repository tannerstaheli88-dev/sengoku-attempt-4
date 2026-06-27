execute as @s[type=husk] at @s run summon giant ~ ~ ~ {Silent:1b,Health:150f,Tags:["ai"]}
execute as @e[type=giant,tag=ai,tag=!stepheight_set] run tag @s add stepheight_set
execute as @e[type=giant,tag=ai,tag=stepheight_set] run attribute @s minecraft:generic.step_height base set 2


execute as @s[type=husk] run tp @s 0 -999 0