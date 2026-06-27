package com.shioh.sengoku.datagen;

import com.shioh.sengoku.Constants;
import com.shioh.sengoku.datagen.advancements.sengokuAdvancementsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.Nullable;

public class sengokuDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator generator) {
    FabricDataGenerator.Pack pack = generator.createPack();
    pack.addProvider(sengokuAdvancementsProvider::new);

  }

  @Override
  public @Nullable String getEffectiveModId() {
    return Constants.MOD_ID;
  }
}