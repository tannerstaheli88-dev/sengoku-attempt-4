## Load entrypoint for the Sengoku Jidai datapack.
## Note: Only comments/whitespace were adjusted for readability. Logic is unchanged.
tellraw @a {"text":"データパックをリロードしました","color":"green"}
execute at @e[type=minecraft:player] run playsound minecraft:entity.player.levelup master @a ~ ~ ~
scoreboard objectives add ambient_cooldown dummy
scoreboard objectives add damage_cooldown dummy
scoreboard objectives add big_stomp dummy
scoreboard objectives add giant_cooldown dummy
scoreboard objectives add yuki-onna_cooldown dummy

team add bear
team add wazowski
team add in.noname
team add oni
team add monkey
team add keneo
team add datsueba
team add wa_nyudo
team add katawaguruma
team add kappa
team add kojin

team modify wazowski nametagVisibility never
team modify bear nametagVisibility never
team modify monkey nametagVisibility never
team modify keneo nametagVisibility never
team modify datsueba nametagVisibility never
team modify wa_nyudo nametagVisibility never
team modify katawaguruma nametagVisibility never
team modify kappa nametagVisibility never
team modify kojin nametagVisibility never
team modify in.noname nametagVisibility never
team modify in.noname color yellow

scoreboard objectives add deaths deathCount
scoreboard objectives add prevDeaths dummy

scoreboard objectives add HP dummy

bossbar add giant_hp {"translate":"sengoku.gashadokuro_hp"}
bossbar set giant_hp max 100
bossbar set giant_hp style progress 
bossbar set giant_hp color yellow

bossbar add ozato {"translate":"sengoku.ozato_hp"}
bossbar set ozato max 500
bossbar set ozato style progress
bossbar set ozato color blue

bossbar add yuki_onna {"translate":"sengoku.yuki_onna_hp"}
bossbar set yuki_onna max 70
bossbar set yuki_onna color blue
bossbar set yuki_onna style progress

bossbar add shinobi {"translate":"sengoku.shinobi_hp"}
bossbar set shinobi max 100
bossbar set shinobi color purple
bossbar set shinobi style progress

bossbar add warlord {"translate":"sengoku.warlord_hp"}
bossbar set warlord max 100
bossbar set warlord color red
bossbar set warlord style progress

bossbar add akugyo {"translate":"sengoku.akugyo_hp"}
bossbar set akugyo max 250
bossbar set akugyo color red
bossbar set akugyo style progress

bossbar add umibozu {"translate":"sengoku.umibozu_hp"}
bossbar set umibozu max 300
bossbar set umibozu color yellow
bossbar set umibozu style progress

scoreboard objectives add shioh.diff dummy

scoreboard players set #Skel_plus shioh.diff 1

function shioh:remove_flame_particle


schedule function #shioh:1sec 1s

scoreboard objectives add multipack.constants dummy
scoreboard objectives add multipack.persist dummy
scoreboard objectives add multipack.temp dummy
scoreboard objectives add multipack.RMB_fungus minecraft.used:minecraft.warped_fungus_on_a_stick

function shioh:wandering_trader/load
