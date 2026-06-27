package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WarmWaterConfigPayload(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WarmWaterConfigPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("warm_water_config"));

    public static final StreamCodec<FriendlyByteBuf, WarmWaterConfigPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.enabled),
        buf -> new WarmWaterConfigPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
