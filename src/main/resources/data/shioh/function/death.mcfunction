tellraw @s {"text":"You died!", "color":"red"}
playsound minecraft:player.death master @s ~ ~ ~ 1 1 1
effect give @s minecraft:blindness 1 1 true
scoreboard players set @s deaths 0