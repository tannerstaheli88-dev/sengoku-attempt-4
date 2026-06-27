package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record DebugDataPayload(java.util.List<String> lines) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DebugDataPayload> TYPE = new CustomPacketPayload.Type<>(sengokuFabric.asId("debug_data"));

    public static final StreamCodec<FriendlyByteBuf, DebugDataPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.lines().size());
            for (String s : payload.lines()) buf.writeUtf(s);
        },
        buf -> {
            int size = buf.readVarInt();
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < size; i++) lines.add(buf.readUtf(32767));
            return new DebugDataPayload(lines);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
