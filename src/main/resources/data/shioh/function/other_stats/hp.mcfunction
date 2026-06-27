execute as @e[type=creeper,tag=!creeper] run data merge entity @s {Health:2.0f,Tags:["creeper"],Attributes:[{Name:generic.max_health,Base:2},{Name:generic.attack_damage,Base:2}]}

execute as @e[type=minecraft:zombie,nbt={IsBaby:1b},tag=!low_hp] run attribute @s minecraft:generic.max_health base set 1
execute as @e[type=minecraft:zombie,nbt={IsBaby:1b},tag=!low_hp] run data merge entity @s {Health:1.0f}
execute as @e[type=minecraft:zombie,nbt={IsBaby:1b},tag=!low_hp] run tag @s add low_hp



tag @s add shioh.checked