execute as @e[type=minecraft:zombie,nbt={IsBaby:1b}] run data merge entity @s {CustomName:'{"text":"Obariyon"}'}
team join in.noname @s

execute as @e[type=zombie,tag=!shioh.checked,nbt={IsBaby:0b}] run attribute @s minecraft:generic.scale base set 1.1

execute as @e[type=zombie,tag=!shioh.checked,predicate=shioh:percentages/50percent,nbt={IsBaby:0b}] run attribute @s minecraft:generic.scale base set 1.2

execute as @e[type=zombie,tag=!shioh.checked,predicate=shioh:percentages/20percent,nbt={IsBaby:0b}] run attribute @s minecraft:generic.scale base set 1.3

execute as @e[type=zombie,tag=!shioh.checked,predicate=shioh:percentages/10percent,nbt={IsBaby:0b}] run attribute @s minecraft:generic.scale base set 1.4

execute as @e[type=zombie,tag=!shioh.checked,nbt={IsBaby:0b},limit=1] unless entity @s[name="Hitotsume nyudo"] unless entity @s[name=Onikuma] unless entity @s[name=Sarugami] if predicate shioh:spawn_chances run item replace entity @s weapon.mainhand with sengoku:stone_kanabo

tag @s add shioh.checked