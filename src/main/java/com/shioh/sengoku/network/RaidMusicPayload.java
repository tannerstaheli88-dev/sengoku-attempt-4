package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * RaidMusicPayload now carries a small integer state:
 * 0 = NONE, 1 = ACTIVE (raid in progress), 2 = VICTORY (raid victory boss bar shown)
 */
public record RaidMusicPayload(int state) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RaidMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("raid_music"));

    public static final StreamCodec<FriendlyByteBuf, RaidMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByte(payload.state()),
        buf -> new RaidMusicPayload(buf.readByte())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
