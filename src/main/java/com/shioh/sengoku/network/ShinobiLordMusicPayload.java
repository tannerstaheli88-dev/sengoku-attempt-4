package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * S2C payload that reports the highest-priority nearby Shinobi Lord music phase.
 * 0 = inactive, 1 = phase one, 2 = phase two.
 */
public record ShinobiLordMusicPayload(int phase) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShinobiLordMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("shinobi_lord_music"));

    public static final StreamCodec<FriendlyByteBuf, ShinobiLordMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeByte(payload.phase()),
        buf -> new ShinobiLordMusicPayload(buf.readUnsignedByte())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}