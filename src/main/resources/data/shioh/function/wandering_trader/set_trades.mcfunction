tag @s add multipack.modified

# Spawns 7 items with nbt containing trades (`@s multipack.temp` is referenced in the trade loot tables)
execute store result score @s multipack.temp run random value 0..7
execute store result score &random multipack.temp run random value 1..4
execute if score &random multipack.temp matches 1 run loot spawn ~ ~ ~ loot shioh:trades/1
execute if score &random multipack.temp matches 2 run loot spawn ~ ~ ~ loot shioh:trades/2
execute if score &random multipack.temp matches 3 run loot spawn ~ ~ ~ loot shioh:trades/3
execute if score &random multipack.temp matches 4 run loot spawn ~ ~ ~ loot shioh:trades/4
scoreboard players reset &random multipack.temp
scoreboard players reset @s multipack.temp

# Transfer items' nbt to storage, then to wandering trader
execute as @e[distance=..0.1,type=item,limit=7,sort=nearest] run function shioh:wandering_trader/store_trade
data modify entity @s Offers.Recipes set from storage shioh:wander_trades Offers.Recipes
data remove storage shioh:wander_trades Offers
