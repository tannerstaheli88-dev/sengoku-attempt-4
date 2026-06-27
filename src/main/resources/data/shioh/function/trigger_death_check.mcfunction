# Optional: limit execution to one player per tick to avoid sound spam in multiplayer
execute as @s at @s if score @s deaths matches 1.. run function shioh:death
scoreboard players set @s deaths 0
