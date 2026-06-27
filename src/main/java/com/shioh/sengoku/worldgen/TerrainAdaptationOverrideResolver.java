package com.shioh.sengoku.worldgen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

public final class TerrainAdaptationOverrideResolver {
    private static final String OVERRIDE_FIELD = "override_terrain_adaptation";
    private static final String NATIVE_FIELD = "terrain_adaptation";

    private TerrainAdaptationOverrideResolver() {
    }

    public static TerrainAdjustment resolveOrDefault(StructurePoolElement element, TerrainAdjustment fallback) {
        return resolve(element).orElse(fallback);
    }

    public static Optional<TerrainAdjustment> resolve(StructurePoolElement element) {
        if (element instanceof TerrainAdaptationOverrideSettings settings) {
            Optional<TerrainAdjustment> override = settings.getTerrainAdjustmentOverride();
            if (override.isPresent()) {
                return override;
            }
        }

        return StructurePoolElement.CODEC.encodeStart(JsonOps.INSTANCE, element)
            .result()
            .flatMap(TerrainAdaptationOverrideResolver::findOverride);
    }

    private static Optional<TerrainAdjustment> findOverride(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            Optional<TerrainAdjustment> directOverride = parseField(jsonObject, OVERRIDE_FIELD)
                .or(() -> parseField(jsonObject, NATIVE_FIELD));
            if (directOverride.isPresent()) {
                return directOverride;
            }

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Optional<TerrainAdjustment> nestedOverride = findOverride(entry.getValue());
                if (nestedOverride.isPresent()) {
                    return nestedOverride;
                }
            }

            return Optional.empty();
        }

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                Optional<TerrainAdjustment> nestedOverride = findOverride(child);
                if (nestedOverride.isPresent()) {
                    return nestedOverride;
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<TerrainAdjustment> parseField(JsonObject jsonObject, String fieldName) {
        if (!jsonObject.has(fieldName)) {
            return Optional.empty();
        }

        JsonElement value = jsonObject.get(fieldName);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return Optional.empty();
        }

        return TerrainAdjustment.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(value.getAsString()))
            .result();
    }
}