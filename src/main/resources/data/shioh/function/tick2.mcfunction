execute as @e[type=minecraft:ender_dragon,tag=!greeted] run function shioh:dragon/200
execute if entity @e[type=minecraft:ender_dragon] run execute as @e[type=minecraft:ender_dragon] store result score @s HP run data get entity @s Health
execute as @e[type=minecraft:ender_dragon,tag=!180] run execute if score @s HP matches ..180 run function shioh:dragon/180
execute as @e[type=minecraft:ender_dragon,tag=!100] run execute if score @s HP matches ..100 run function shioh:dragon/100
execute as @e[type=minecraft:ender_dragon,tag=!40] run execute if score @s HP matches ..40 run function shioh:dragon/40
execute as @e[type=minecraft:ender_dragon,tag=!20] run execute if score @s HP matches ..20 run function shioh:dragon/20
execute as @e[type=minecraft:ender_dragon,tag=!5] run execute if score @s HP matches ..5 run function shioh:dragon/5

execute as @e[type=warden] at @s if entity @a[distance=..70] run function shioh:advancements/ozato
execute as @e[type=minecraft:piglin] unless entity @s[nbt={CustomName:'{"translate":"entity.sengoku.datsueba","color":"white"}'}] run team join keneo @s
execute as @e[type=minecraft:strider] unless entity @s[nbt={CustomName:'{"translate":"entity.sengoku.katawaguruma","color":"white"}'}] run team join wa_nyudo @s