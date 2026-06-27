
execute as @e[type=minecraft:parrot,nbt={Variant:0}] run data modify entity @s CustomName set value '{"translate":"entity.sengoku.bird.daurian_redstart","color":"white"}'
execute as @e[type=minecraft:parrot,nbt={Variant:1}] run data modify entity @s CustomName set value '{"translate":"entity.sengoku.bird.blue_flycatcher","color":"white"}'
execute as @e[type=minecraft:parrot,nbt={Variant:2}] run data modify entity @s CustomName set value '{"translate":"entity.sengoku.bird.warbling_white_eye","color":"white"}'
execute as @e[type=minecraft:parrot,nbt={Variant:3}] run data modify entity @s CustomName set value '{"translate":"entity.sengoku.bird.black_naped_oriole","color":"white"}'
execute as @e[type=minecraft:parrot,nbt={Variant:4}] run data modify entity @s CustomName set value '{"translate":"entity.sengoku.bird.long_tailed_tit","color":"white"}'

tag @s add shioh.checked
team join in.noname @s