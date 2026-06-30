execute as @e[type=magma_cube,nbt={Size:0}] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.2 0.3 0.2 0 1 normal
execute as @e[type=magma_cube,nbt={Size:1}] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.3 0.3 0.3 0 3 normal
execute as @e[type=magma_cube,nbt={Size:2}] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.5 0.3 0.5 0 5 normal
execute as @e[type=magma_cube,nbt={Size:3}] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.6 0.3 0.6 0 7 normal
execute as @e[type=magma_cube,nbt={Size:0}] at @s run particle minecraft:sculk_soul ~ ~0.55 ~ 0.2 0.3 0.2 0 1 normal
execute as @e[type=magma_cube,nbt={Size:1}] at @s run particle minecraft:sculk_soul ~ ~0.55 ~ 0.3 0.3 0.3 0 3 normal
execute as @e[type=magma_cube,nbt={Size:2}] at @s run particle minecraft:sculk_soul ~ ~0.55 ~ 0.5 0.3 0.5 0 5 normal
execute as @e[type=magma_cube,nbt={Size:3}] at @s run particle minecraft:sculk_soul ~ ~0.55 ~ 0.6 0.3 0.6 0 7 normal

execute as @e[type=skeleton_horse] at @s run particle minecraft:trial_spawner_detection_ominous ~ ~0.55 ~ 0.2 0.3 0.6 0 3 normal
execute as @e[type=skeleton_horse] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.2 0.3 0.6 0 3 normal
execute as @e[type=zombie_horse] at @s run particle minecraft:poof ~ ~0.55 ~ 0.3 0.3 0.3 0 3 normal
execute as @e[type=breeze] at @s run particle minecraft:white_smoke ~ ~0.55 ~ 0.3 0.3 0.3 0 3 normal

execute as @e[type=strider] at @s run particle minecraft:large_smoke ~ ~0.55 ~ 0.6 0.3 0.6 0 5 normal
execute as @e[type=strider,predicate=shioh:dimension/nether] at @s run particle minecraft:trial_spawner_detection_ominous ~ ~0.55 ~ 0.6 0.3 0.6 0 8 normal
execute as @e[type=strider,predicate=shioh:dimension/overworld] at @s run particle minecraft:trial_spawner_detection ~ ~0.55 ~ 0.6 0.3 0.6 0 8 normal

execute as @e[type=sengoku:warlord,tag=finale] at @s run particle minecraft:electric_spark ~ ~1.85 ~ 0.2 0.3 0.2 0 1 normal

# Play death sound for players whose deaths increased (per-player)
execute as @a if score @s deaths > @s prevDeaths run playsound minecraft:player.death master @s ~ ~ ~ 1 1 1
# Update prevDeaths for each player individually
execute as @a run scoreboard players operation @s prevDeaths = @s deaths

execute as @e[type=sengoku:maikubi] at @s run particle minecraft:trial_spawner_detection_ominous ~ ~0.55 ~ 0.2 0.3 0.6 0 3 normal

execute as @e[type=wither] at @s run particle minecraft:large_smoke ~ ~1.55 ~ 0.5 0.3 0.5 0 5 normal


execute as @e[type=sengoku:umi_bozu] at @s run particle sengoku:dragon_splash ~ ~0 ~ 1.1 0.3 1.1 0 1 normal
execute as @e[type=sengoku:umi_bozu] at @s run particle minecraft:splash ~ ~1.55 ~ 1.5 0.3 1.5 0 5 normal

execute as @e[type=husk] at @s run particle minecraft:falling_nectar ~ ~1.55 ~ 0.4 0.24 0.4 0 3 normal
