## Loop every 22min. With a 1/16 chance, spawn a wandering trader near a random player
schedule function shioh:wandering_trader/spawn_loop 1320s
execute if predicate shioh:chance/6.25_percent in overworld at @r[distance=0..] positioned over world_surface run summon wandering_trader ~ ~ ~ {DespawnDelay:48000,Tags:["multipack.trader_spawn"]}
execute as @e[type=wandering_trader,tag=multipack.trader_spawn] at @s run spreadplayers ~ ~ 16 48 false @s
execute at @e[type=wandering_trader,tag=multipack.trader_spawn] run summon trader_llama ~ ~ ~ {Tags:["multipack.trader_spawn"]}
execute at @e[type=wandering_trader,tag=multipack.trader_spawn] run summon trader_llama ~ ~ ~ {Tags:["multipack.trader_spawn"]}
execute as @e[type=trader_llama,tag=multipack.trader_spawn] at @s run data modify entity @s leash.UUID set from entity @e[type=wandering_trader,tag=multipack.trader_spawn,limit=1] UUID
tag @e[type=wandering_trader,tag=multipack.trader_spawn] remove multipack.trader_spawn
tag @e[type=trader_llama,tag=multipack.trader_spawn] remove multipack.trader_spawn
