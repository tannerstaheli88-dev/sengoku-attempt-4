execute as @e[type=zombie,tag=!shioh.checked,tag=!global.ignore] run function shioh:zombie/tag
execute as @e[type=drowned,tag=!shioh.checked,tag=!global.ignore] run function shioh:kappa/tag
execute as @e[type=enderman,tag=!shioh.checked,tag=!global.ignore] run function shioh:enderman/tag
execute as @e[type=glow_squid,tag=!shioh.checked,tag=!global.ignore] run function shioh:glowsquid/tag
execute as @e[type=sengoku:warlord,tag=!shioh.checked,tag=!global.ignore] run function shioh:kensei/tag
execute as @e[type=vindicator,tag=!shioh.checked,tag=!global.ignore] run function shioh:kensei/tag
execute as @e[type=sengoku:sarugami,tag=!shioh.checked,tag=!global.ignore] run function shioh:monkey/tag
execute as @e[type=pillager,tag=!shioh.checked,tag=!global.ignore] run function shioh:kensei/tag
execute as @e[type=evoker,tag=!shioh.checked,tag=!global.ignore] run function shioh:kensei/tag
execute as @e[type=illusioner,tag=!shioh.checked,tag=!global.ignore] run function shioh:shinobi/tag
execute as @e[type=sengoku:shinobi_lord,tag=!shioh.checked,tag=!global.ignore] run function shioh:shinobi/tag
execute as @e[type=shulker,tag=!shioh.checked,tag=!global.ignore] run function shioh:shulker/tag
execute as @e[type=piglin,tag=!shioh.checked,tag=!global.ignore] run function shioh:piglin/tag
execute as @e[type=parrot,tag=!shioh.checked,tag=!global.ignore] run function shioh:bird/tag
execute as @e[type=strider,tag=!shioh.checked,tag=!global.ignore] run function shioh:strider/tag
execute as @e[type=skeleton_horse,tag=!shioh.checked,tag=!global.ignore] run function shioh:horse/tag
execute as @e[type=llama,tag=!shioh.checked,tag=!global.ignore] run function shioh:deer/tag
execute as @e[type=bee,tag=!shioh.checked,tag=!global.ignore] run function shioh:bee/tag
execute as @e[type=zombie_horse,tag=!shioh.checked,tag=!global.ignore] run function shioh:horse/tag
execute as @e[type=tropical_fish,tag=!shioh.checked,tag=!global.ignore] run function shioh:fish/tag
execute as @e[type=zombified_piglin,tag=!shioh.checked,tag=!global.ignore] run function shioh:piglin/shikome
execute as @e[type=piglin_brute,tag=!shioh.checked,tag=!global.ignore] run function shioh:piglin/gozuki
execute as @e[type=cave_spider,tag=!shioh.checked,tag=!global.ignore] run function shioh:spider/tsuchigumo


execute as @e[type=vex,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag
execute as @e[type=warden,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag
execute as @e[type=armadillo,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag
execute as @e[type=iron_golem,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag
execute as @e[type=drowned,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag
execute as @e[type=panda,tag=!shioh.checked,tag=!global.ignore] run function shioh:sizes/tag

execute as @e[type=creeper,tag=!shioh.checked,tag=!global.ignore] run function shioh:other_stats/hp
execute as @e[type=skeleton,tag=!shioh.checked,tag=!global.ignore] run function shioh:other_stats/hand_items

execute as @e[type=hoglin,tag=!shioh.checked,tag=!global.ignore] run function shioh:other_stats/baby_hate
execute as @e[type=zoglin,tag=!shioh.checked,tag=!global.ignore] run function shioh:other_stats/baby_hate
execute as @e[type=husk,tag=!shioh.checked,tag=!global.ignore] run function shioh:other_stats/baby_hate
execute as @e[type=minecraft:villager,tag=female,nbt={VillagerData:{profession:"minecraft:cleric"}},predicate=!shioh:dimension/nether] run data merge entity @s {CustomName:'{"translate":"entity.sengoku.miko"}'}
execute as @e[type=minecraft:villager,predicate=shioh:dimension/nether] run data merge entity @s {CustomName:'{"translate":"entity.sengoku.shiryo"}'}

schedule function #shioh:1sec 1s
