package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Simple S2C payload to notify clients that a nearby Wither (Tatarigami) fight is active
 */
public record TatarigamiMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TatarigamiMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("tatarigami_music"));

    public static final StreamCodec<FriendlyByteBuf, TatarigamiMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active),
        buf -> new TatarigamiMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}