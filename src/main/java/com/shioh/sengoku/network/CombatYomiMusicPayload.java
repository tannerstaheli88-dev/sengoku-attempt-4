package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record CombatYomiMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CombatYomiMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("yomi_music"));

    public static final StreamCodec<FriendlyByteBuf, CombatYomiMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new CombatYomiMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
