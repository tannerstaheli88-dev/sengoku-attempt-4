package com.shioh.sengoku.mixin.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(BlockEntityRenderers.class)
public class BlockEntityRenderersMixin {

    @Shadow @Final private static Map<BlockEntityType<?>, BlockEntityRendererProvider<?>> PROVIDERS;

    @Inject(method = "register", at = @At("TAIL"))
    private static <T extends BlockEntity>  void injectedRegisterAtTail(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> factory, CallbackInfo ci) {
            PROVIDERS.remove(BlockEntityType.BED);
    }
}
