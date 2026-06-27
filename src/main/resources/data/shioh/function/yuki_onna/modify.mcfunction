# from: entity/mobs/init
# @s: dune blaze

tag @s add in.ticking_entity
tag @s add in.dune_blaze
team join in.noname @s

item replace entity @e[type=stray,name=Yuki-Onna] weapon.mainhand with diamond_sword[minecraft:custom_name='"Icicle Sword"'] 1
item replace entity @e[type=stray,name=Yuki-Onna] armor.head with diamond_helmet[minecraft:custom_name='"Invisible Helmet"'] 1
item replace entity @e[type=stray,name=Yuki-Onna] armor.chest with minecraft:diamond_chestplate[minecraft:custom_name='"Invisible Chestplate"'] 1
item replace entity @e[type=stray,name=Yuki-Onna] armor.legs with diamond_leggings[minecraft:custom_name='"Invisible Leggings"'] 1
item replace entity @e[type=stray,name=Yuki-Onna] armor.feet with diamond_boots[minecraft:custom_name='"Invisible Boots"'] 1
execute as @e[type=minecraft:stray,name="Yuki-Onna"] run attribute @s minecraft:generic.follow_range base set 64
execute as @e[type=minecraft:stray,name="Yuki-Onna"] run effect give @s minecraft:speed 1 0 true