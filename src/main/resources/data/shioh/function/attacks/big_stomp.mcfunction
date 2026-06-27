execute as @e[type=minecraft:giant,scores={big_stomp=25}] run effect give @s levitation 1 5 true
execute as @e[type=minecraft:giant,scores={big_stomp=55}] run effect give @s slow_falling 2 0 true
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=player,distance=..8,gamemode=survival] at @s run damage @s 10 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=iron_golem,distance=..15] at @s run damage @s 30 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=wolf,distance=..8] at @s run damage @s 10 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=snow_golem,distance=..10] at @s run damage @s 5 minecraft:mob_attack by @e[type=giant,sort=nearest,limit=1]
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=player,distance=..8,gamemode=survival] at @s run effect give @s levitation 1 12 true
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=iron_golem,distance=..15] at @s run effect give @s levitation 1 20 true
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=wolf,distance=..8] at @s run effect give @s levitation 1 20 true
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run execute as @e[type=snow_golem,distance=..10] at @s run effect give @s levitation 1 20 true
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run playsound minecraft:entity.zombie.attack_iron_door hostile @a ~ ~ ~ 1 0.5
execute as @e[type=minecraft:giant,scores={big_stomp=60},nbt={OnGround:1b}] run particle minecraft:explosion ~ ~ ~ 3.25 0.25 3.25 0 55
