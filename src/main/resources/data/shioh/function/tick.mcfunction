## Tick entrypoint for the Sengoku Jidai datapack.
## Note: Only comments/whitespace were adjusted for readability. Logic is unchanged.
tag @e[type=sengoku:shinobi_lord] add shinobi_boss
tag @e[type=illusioner,nbt={ArmorItems:[{},{},{},{id:"minecraft:white_concrete"}]}] add sengoku_elite

bossbar set giant_hp players @a
execute at @a run execute as @e[type=giant,distance=..60] run execute store result bossbar giant_hp value run data get entity @s Health
execute at @a if entity @e[type=giant,distance=..60] run bossbar set giant_hp visible true
execute at @a if entity @e[type=giant,distance=60..] run bossbar set giant_hp visible false
execute at @a unless entity @e[type=giant,distance=..60] run bossbar set giant_hp visible false

bossbar set umibozu players @a
execute at @a run execute as @e[type=sengoku:umi_bozu,distance=..60] run execute store result bossbar umibozu value run data get entity @s Health
execute at @a if entity @e[type=sengoku:umi_bozu,distance=..60] run bossbar set umibozu visible true
execute at @a if entity @e[type=sengoku:umi_bozu,distance=60..] run bossbar set umibozu visible false
execute at @a unless entity @e[type=sengoku:umi_bozu,distance=..60] run bossbar set umibozu visible false

bossbar set ozato players @a
execute at @a run execute as @e[type=warden,distance=..30] run execute store result bossbar ozato value run data get entity @s Health
execute at @a if entity @e[type=warden,distance=..30] run bossbar set ozato visible true
execute at @a if entity @e[type=warden,distance=30..] run bossbar set ozato visible false
execute at @a unless entity @e[type=warden,distance=..30] run bossbar set ozato visible false

bossbar set yuki_onna players @a
execute at @a run execute as @e[type=sengoku:yuki_onna,distance=..20] run execute store result bossbar yuki_onna value run data get entity @s Health
execute at @a if entity @e[type=sengoku:yuki_onna,distance=..20] run bossbar set yuki_onna visible true
execute at @a if entity @e[type=sengoku:yuki_onna,distance=20..] run bossbar set yuki_onna visible false
execute at @a unless entity @e[type=sengoku:yuki_onna,distance=..20] run bossbar set yuki_onna visible false

bossbar set shinobi players @a
execute at @a run execute as @e[type=sengoku:shinobi_lord,tag=shinobi_boss,distance=..20] run execute store result bossbar shinobi value run data get entity @s Health
execute at @a if entity @e[type=sengoku:shinobi_lord,tag=shinobi_boss,distance=..20] run bossbar set shinobi visible true
execute at @a if entity @e[type=sengoku:shinobi_lord,tag=shinobi_boss,distance=20..] run bossbar set shinobi visible false
execute at @a unless entity @e[type=sengoku:shinobi_lord,tag=shinobi_boss,distance=..20] run bossbar set shinobi visible false

bossbar set warlord players @a
execute at @a run execute as @e[type=sengoku:warlord,distance=..10] run execute store result bossbar warlord value run data get entity @s Health
execute at @a if entity @e[type=sengoku:warlord,distance=..10] run bossbar set warlord visible true
execute at @a if entity @e[type=sengoku:warlord,distance=10..] run bossbar set warlord visible false
execute at @a unless entity @e[type=sengoku:warlord,distance=..10] run bossbar set warlord visible false

bossbar set akugyo players @a
execute at @a run execute as @e[type=sengoku:akugyo,distance=..40,predicate=!shioh:end] run execute store result bossbar akugyo value run data get entity @s Health
execute at @a if entity @e[type=sengoku:akugyo,distance=..40,predicate=!shioh:end] run bossbar set akugyo visible true
execute at @a if entity @e[type=sengoku:akugyo,distance=40,predicate=!shioh:end] run bossbar set akugyo visible false
execute at @a unless entity @e[type=sengoku:akugyo,distance=..40,predicate=!shioh:end] run bossbar set akugyo visible false


execute if entity @e[type=minecraft:giant] run execute at @e[type=minecraft:giant,nbt={HurtTime:10s}] run playsound entity.skeleton.hurt hostile @a[distance=..150] ~ ~ ~ 100 0.2

execute as @e[tag=brain] run function shioh:giant_death

# Kills all entities tagged as "played."
kill @e[tag=played]

# Stores the health of each giant into its "HP" scoreboard value.
execute if entity @e[type=minecraft:giant] run execute as @e[type=minecraft:giant] store result score @s HP run data get entity @s Health

# Executes a function for giants without the "ai" tag.
execute if entity @e[type=minecraft:giant,tag=!ai] run execute as @e[type=minecraft:giant,tag=!ai] run execute at @e[type=minecraft:giant,tag=!ai] run function shioh:giant

# Sets the damage cooldown to 50 for giants with the "ai" tag if their cooldown is less than or equal to 1.
execute if score @e[type=giant,tag=ai,limit=1] damage_cooldown matches ..1 run scoreboard players set @e[type=giant] damage_cooldown 50

# Executes an area damage attack for giants whose damage cooldown is 25.
execute if entity @e[type=minecraft:giant] run execute at @e[type=minecraft:giant,scores={damage_cooldown=25}] run function shioh:attacks/area_damage

# Decreases the damage cooldown by 1 for all giants.
execute if entity @e[type=minecraft:giant] run scoreboard players remove @e[type=giant] damage_cooldown 1

# Resets the "big_stomp" score for giants when it reaches 500 or higher.
execute if entity @e[type=minecraft:giant] run execute if score @e[type=giant,limit=1] big_stomp matches 500.. run scoreboard players reset @e[type=giant] big_stomp

# Executes a "big stomp" attack for giants whose "big_stomp" score is between 25 and 60.
execute if entity @e[type=minecraft:giant] run execute at @e[type=minecraft:giant,scores={big_stomp=25..60}] run function shioh:attacks/big_stomp

# Increases the "big_stomp" score by 1 for all giants.
execute if entity @e[type=minecraft:giant] run scoreboard players add @e[type=giant] big_stomp 1

# Resets the "skeleton_cooldown" score for giants when it reaches 1250 or higher.
execute if entity @e[type=minecraft:giant] run execute if score @e[type=giant,limit=1] skeleton_cooldown matches 1250.. run scoreboard players reset @e[type=giant] skeleton_cooldown

# Executes a skeleton summoning attack for giants whose "skeleton_cooldown" score is between 250 and 300.
execute if entity @e[type=minecraft:giant] run execute at @e[type=minecraft:giant,scores={skeleton_cooldown=250..300}] run function shioh:attacks/skeletons

# Increases the "skeleton_cooldown" score by 1 for all giants.
execute if entity @e[type=minecraft:giant] run scoreboard players add @e[type=giant] skeleton_cooldown 1

# Stores the health of each giant into its "HP" scoreboard value (duplicate of an earlier command).
execute if entity @e[type=minecraft:giant] run execute as @e[type=minecraft:giant] store result score @s HP run data get entity @s Health

# Gives resistance effect to giants with health less than or equal to 25 HP.
execute if entity @e[type=minecraft:giant] run execute as @e[type=minecraft:giant] if score @s HP matches ..25 run effect give @s minecraft:resistance infinite 2 true

# Executes a final attack function for giants with less than or equal to 25 HP and without the "finale" tag.
execute as @e[type=giant,tag=!finale] run execute if score @s HP matches ..25 run function shioh:attacks/final

# Gives invisibility and resistance effects to entities tagged as "brain."
effect give @e[tag=brain] minecraft:invisibility infinite 255 true
effect give @e[tag=brain] minecraft:resistance infinite 255 true

# Resets the "ambient_cooldown" score for giants when it reaches 300 or higher.
execute if score @e[type=giant,tag=ai,limit=1] ambient_cooldown matches 300.. run scoreboard players reset @e[type=giant] ambient_cooldown

# Increases the "ambient_cooldown" score by 1 for all giants with the "ai" tag.
scoreboard players add @e[type=minecraft:giant,tag=ai] ambient_cooldown 1

# Plays a zombie ambient sound near giants with an ambient cooldown of exactly 1 within a radius of 50 blocks from players.
execute if entity @e[type=minecraft:giant,tag=ai] run execute at @e[type=minecraft:giant,scores={ambient_cooldown=1}] run playsound minecraft:entity.skeleton.ambient master @a[distance=..50] ~ ~ ~ 100 0.2

# Tags skeletons that haven't been processed yet as "progressing."
execute as @e[type=minecraft:husk,tag=!process,tag=!checked] run tag @s add progressing

# Tags progressing skeletons that meet specific predicates (height check and plains biome) as "process."
execute as @e[type=minecraft:husk,tag=!process,tag=progressing,predicate=shioh:5,predicate=shioh:height_check,predicate=shioh:plains] run tag @s add process

# Executes a giant summoning function for random skeletons marked as "process," meeting specific predicates (height check and night).
execute as @e[distance=0..,type=minecraft:husk,sort=random,tag=process,predicate=shioh:height_check] if score @a[limit=1] giant_cooldown matches -1 run execute if predicate shioh:5 run execute if predicate shioh:night run execute at @s run execute unless entity @e[type=giant] run function shioh:giant

# Tags progressing skeletons as checked after processing them.
execute as @e[type=minecraft:husk,tag=progressing] run tag @s add checked

# Removes the progressing tag from skeletons that have been checked.
execute as @e[type=minecraft:husk,tag=progressing, tag=checked] run tag @s remove progressing

# Resets the giant cooldown for players when it reaches zero or below.
execute if score @a[limit=1] giant_cooldown matches ..0 run scoreboard players reset @a giant_cooldown

# Decreases the giant cooldown by one for all players.
execute as @a run scoreboard players remove @a giant_cooldown 1

execute if entity @e[type=sengoku:warlord] run execute as @e[type=sengoku:warlord] store result score @s HP run data get entity @s Health
execute as @e[type=sengoku:warlord,tag=!finale] run execute if score @s HP matches ..50 run function shioh:kensei/final
execute if entity @e[type=sengoku:warlord] run execute as @e[type=sengoku:warlord] if score @s HP matches ..25 run effect give @s minecraft:resistance infinite 1 true
execute if entity @e[type=sengoku:warlord] run effect give @s minecraft:fire_resistance 2 1 true
execute at @e[type=sengoku:warlord,tag=finale] if entity @a[distance=..35] run weather thunder 10s
execute as @e[type=sengoku:warlord,tag=finale] at @s if predicate shioh:percentages/1percent run playsound minecraft:entity.lightning_bolt.thunder hostile @a[distance=..50] ~ ~ ~
execute at @e[type=sengoku:umi_bozu] if entity @a[distance=..35] run weather rain 10s

# Gives speed effect to skeleton horses near entities tagged as "horseman."
execute at @e[tag=horseman] run effect give @e[type=minecraft:skeleton_horse,distance=..1] minecraft:speed 3 4 true

# These commands are now handled efficiently by the mod for better performance
# effect give @e[type=minecraft:panda] minecraft:speed 2 3 true
# effect give @e[type=minecraft:llama] minecraft:speed 4 5 true

# effect give @e[type=minecraft:skeleton_horse] minecraft:speed 3 4 true
# execute as @e[type=skeleton_horse] run effect give @s minecraft:fire_resistance 2 1 true
effect give @e[type=minecraft:camel] minecraft:speed 4 5 true

execute as @e[type=minecraft:parrot] run attribute @s minecraft:generic.flying_speed base set 44.6
execute as @e[type=sengoku:crow] run attribute @s minecraft:generic.flying_speed base set 44.6
execute as @e[type=pillager] run data modify entity @s HandItems[1] set value {id:"minecraft:spectral_arrow",Count:1}
execute as @e[type=minecraft:piglin,nbt={HandItems:[{id:"minecraft:crossbow"},{}]}] run data modify entity @s HandItems[0] set value {id:"sengoku:stone_kanabo",Count:1}

function shioh:remove_flame_particle
function shioh:tick2


execute as @e[type=minecraft:villager,tag=!shioh.checked,tag=!global.ignore] at @s if predicate shioh:percentages/50percent run function shioh:villager/tag

execute as @e[tag=female] run data merge entity @s {Silent:1b}

# Only play sound for Silent:1 villagers with shioh:1percent=1
execute as @e[type=minecraft:villager,nbt={Silent:1b},tag=female1,predicate=!shioh:dimension/nether] at @s if predicate shioh:women_voice run playsound minecraft:entity.villager.ambient2 neutral @a[distance=..16] ~ ~ ~ 1 1
execute as @e[type=minecraft:villager,nbt={Silent:1b},tag=female2,predicate=!shioh:dimension/nether] at @s if predicate shioh:women_voice run playsound minecraft:entity.villager.ambient3 neutral @a[distance=..16] ~ ~ ~ 1 1
execute as @e[type=minecraft:iron_golem] at @s if predicate shioh:golem_voice run playsound minecraft:entity.iron_golem.idle neutral @a[distance=..16] ~ ~ ~ 1 1
execute as @e[type=minecraft:villager,nbt={Silent:1b,HurtTime:10s},predicate=!shioh:dimension/nether] at @s run playsound minecraft:entity.villager.hurt2 neutral @a[distance=..16] ~ ~ ~ 1 1




