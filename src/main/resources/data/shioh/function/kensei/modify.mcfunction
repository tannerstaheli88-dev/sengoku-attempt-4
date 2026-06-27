
# Run once per Vindicator named Kensei (not yet checked)

# Helmet check
execute if data entity @s {ArmorItems:[{},{},{},{id:"minecraft:white_banner"}]} run item replace entity @s armor.head with sengoku:daimyo_kabuto

# Chestplate check
execute if data entity @s {ArmorItems:[{},{},{id:"minecraft:white_wool"},{}]} run item replace entity @s armor.chest with sengoku:daimyo_do

# Leggings check
execute if data entity @s {ArmorItems:[{},{id:"minecraft:white_terracotta"},{},{}]} run item replace entity @s armor.legs with sengoku:daimyo_haidate

# Boots check
execute if data entity @s {ArmorItems:[{id:"minecraft:white_concrete"},{},{},{}]} run item replace entity @s armor.feet with sengoku:daimyo_kusazuri

# Weapon (always replaced)
item replace entity @s weapon.mainhand with sengoku:blade_of_the_kensei

# Scale + team
team join in.noname @s

# Done, mark processed
tag @s add shioh.checked
