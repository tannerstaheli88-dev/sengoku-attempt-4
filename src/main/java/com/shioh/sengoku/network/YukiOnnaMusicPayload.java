package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Simple S2C payload to notify clients that a nearby Yuki Onna is active (stalking/aggro)
 */
public record YukiOnnaMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<YukiOnnaMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("yuki_onna_music"));

    public static final StreamCodec<FriendlyByteBuf, YukiOnnaMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active),
        buf -> new YukiOnnaMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
