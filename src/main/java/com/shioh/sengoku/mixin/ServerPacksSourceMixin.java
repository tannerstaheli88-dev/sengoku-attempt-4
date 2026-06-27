package com.shioh.sengoku.mixin;

import com.shioh.sengoku.materialpack.BuiltinModDatapackLoader;
import com.shioh.sengoku.materialpack.ResourceAndDatapackCustomLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Part of the Material Pack system
 * This mixin lets us load datapacks globally from config/sengoku/bwmp_data
 * instead of having to include a datapack in each world
 **/
@Mixin(ServerPacksSource.class)
public class ServerPacksSourceMixin {
  @ModifyArg(
      method = "createPackRepository",
      at = @At(
          value = "INVOKE",
          target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"
      )
  )
  private static RepositorySource[] addMaterialDataPackFinder(RepositorySource[] original) {
    ResourceAndDatapackCustomLoader finder = new ResourceAndDatapackCustomLoader(PackType.SERVER_DATA, true);
    RepositorySource[] withCustomFolder = ArrayUtils.add(original, finder);
    return ArrayUtils.add(withCustomFolder, new BuiltinModDatapackLoader());
  }
} 