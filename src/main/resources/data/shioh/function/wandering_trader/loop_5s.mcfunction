## Loop every 5s. Update all unmodified Wander Traders
schedule function shioh:wandering_trader/loop_5s 1s
execute as @e[type=wandering_trader,tag=!multipack.modified] at @s run function shioh:wandering_trader/set_trades
