package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record VillageMusicPayload(boolean inside) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VillageMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("village_music"));

    public static final StreamCodec<FriendlyByteBuf, VillageMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.inside),
        buf -> new VillageMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
