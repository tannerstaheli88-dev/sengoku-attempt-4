package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayerPoisePayload(float current, float max) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerPoisePayload> TYPE =
        new CustomPacketPayload.Type<>(sengokuFabric.asId("player_poise"));

    public static final StreamCodec<FriendlyByteBuf, PlayerPoisePayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeFloat(payload.current());
            buf.writeFloat(payload.max());
        },
        buf -> new PlayerPoisePayload(buf.readFloat(), buf.readFloat())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
