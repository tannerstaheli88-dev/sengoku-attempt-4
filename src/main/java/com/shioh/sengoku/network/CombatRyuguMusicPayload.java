package com.shioh.sengoku.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record CombatRyuguMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CombatRyuguMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("ryugu_music"));

    public static final StreamCodec<FriendlyByteBuf, CombatRyuguMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new CombatRyuguMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
