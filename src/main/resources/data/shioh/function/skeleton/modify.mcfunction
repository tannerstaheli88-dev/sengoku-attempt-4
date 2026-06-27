execute store result score $server_difficulty shioh.diff run difficulty
item replace entity @s weapon.mainhand with iron_sword
item replace entity @s weapon.offhand with shield
data merge entity @s {DeathLootTable:"shioh:skeleton_with_sword"}
execute if score $server_difficulty shioh.diff matches 3.. if predicate shioh:enchant_chances run item modify entity @s weapon.mainhand shioh:skeleton/modify

## Random Armor Chance
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"leather_boots",Count:1},{id:"leather_leggings",Count:1},{id:"leather_chestplate",Count:1},{id:"leather_helmet",Count:1}]}
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"iron_boots",Count:1},{id:"iron_leggings",Count:1},{id:"iron_helmet",Count:1}]}
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"golden_leggings",Count:1},{id:"golden_chestplate",Count:1},{id:"golden_helmet",Count:1}]}
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"iron_helmet",Count:1}]}
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"iron_helmet",Count:1}]}
execute if predicate shioh:random_armor_chance run data merge entity @s {ArmorItems:[{id:"iron_helmet",Count:1}]}