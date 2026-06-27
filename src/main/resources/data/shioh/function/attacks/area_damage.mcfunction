execute as @e[type=player,distance=..6] at @s run damage @s 8 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=wolf,distance=..8] at @s run damage @s 5 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=iron_golem,distance=..15] at @s run damage @s 10 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=snow_golem,distance=..10] at @s run damage @s 1 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=minecraft:giant] run scoreboard players add @s[type=minecraft:giant,tag=ai] damage_cooldown 30