package com.shioh.sengoku.network;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.shioh.sengoku.sengokuFabric;

public record CastleMusicPayload(boolean inside) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CastleMusicPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("castle_music"));

    public static final StreamCodec<FriendlyByteBuf, CastleMusicPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.inside()),
        buf -> new CastleMusicPayload(buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
