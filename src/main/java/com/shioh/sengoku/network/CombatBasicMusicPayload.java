package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record CombatBasicMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CombatBasicMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("combat_basic_music"));

    public static final StreamCodec<FriendlyByteBuf, CombatBasicMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new CombatBasicMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
