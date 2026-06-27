package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record StealthCooldownPayload(int ticks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StealthCooldownPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("stealth_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, StealthCooldownPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeInt(payload.ticks()),
        buf -> new StealthCooldownPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
