package com.shioh.sengoku.network;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.shioh.sengoku.sengokuFabric;

public record CastleCombatMusicPayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CastleCombatMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("castle_combat_music"));

    public static final StreamCodec<FriendlyByteBuf, CastleCombatMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.active()),
        buf -> new CastleCombatMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
