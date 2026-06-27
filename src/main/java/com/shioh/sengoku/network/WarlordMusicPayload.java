package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C payload that reports the highest-priority nearby Warlord music phase.
 * 0 = inactive, 1 = phase one, 2 = phase two.
 */
public record WarlordMusicPayload(int phase) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WarlordMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("warlord_music"));

    public static final StreamCodec<FriendlyByteBuf, WarlordMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByte(payload.phase()),
        buf -> new WarlordMusicPayload(buf.readUnsignedByte())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
