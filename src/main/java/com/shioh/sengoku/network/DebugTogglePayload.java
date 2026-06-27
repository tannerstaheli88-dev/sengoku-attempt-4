package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DebugTogglePayload(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DebugTogglePayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("debug_toggle"));

    public static final StreamCodec<FriendlyByteBuf, DebugTogglePayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.enabled()),
        buf -> new DebugTogglePayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
