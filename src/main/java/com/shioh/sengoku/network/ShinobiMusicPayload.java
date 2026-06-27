package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record ShinobiMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShinobiMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("shinobi_music"));

    public static final StreamCodec<FriendlyByteBuf, ShinobiMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new ShinobiMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}