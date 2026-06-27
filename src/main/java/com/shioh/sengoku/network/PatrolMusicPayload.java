package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record PatrolMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PatrolMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("patrol_music"));

    public static final StreamCodec<FriendlyByteBuf, PatrolMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new PatrolMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
