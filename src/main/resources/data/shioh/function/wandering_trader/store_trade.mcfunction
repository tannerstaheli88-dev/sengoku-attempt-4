## Transfer this item's tag data into storage then kill this item
data modify storage shioh:wander_trades Offers.Recipes append from entity @s Item.components."minecraft:custom_data"
kill @s
