package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.KobayakawaAshigaruEntity;
import com.shioh.sengoku.entity.KobayakawaSamuraiEntity;
import com.shioh.sengoku.entity.KobayakawaSoheiEntity;
import com.shioh.sengoku.entity.SatomiAshigaruEntity;
import com.shioh.sengoku.entity.SatomiSamuraiEntity;
import com.shioh.sengoku.entity.SatomiSoheiEntity;
import com.shioh.sengoku.entity.TakedaAshigaruEntity;
import com.shioh.sengoku.entity.TakedaSamuraiEntity;
import com.shioh.sengoku.entity.TakedaSoheiEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace ominous banners with custom patterned black banners on ALL mobs.
 * This affects pillagers, vindicators, evokers, and other illagers that spawn as raid captains.
 */
@Mixin(Mob.class)
public abstract class OminousBannerReplacementMixin {
    
    @Unique
    private boolean sengoku$isReplacingBanner = false;
    
    /**
     * Intercept when any mob has an item set in the head slot.
     * If it's a banner, replace it with a custom patterned black banner.
     */
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void replaceBannerInHead(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        // Prevent infinite recursion
        if (sengoku$isReplacingBanner) {
            return;
        }
        
        // Only process head slot with banners
        if (slot == EquipmentSlot.HEAD && stack.getItem() instanceof BannerItem) {
            // Cancel the original call
            ci.cancel();
            
            // Set flag to prevent recursion
            sengoku$isReplacingBanner = true;
            
            // Get mob reference
            Mob mob = (Mob)(Object)this;
            
            // Determine clan type
            boolean isSatomi = mob instanceof SatomiAshigaruEntity || 
                              mob instanceof SatomiSamuraiEntity || 
                              mob instanceof SatomiSoheiEntity;
            
            boolean isTakeda = mob instanceof TakedaAshigaruEntity || 
                              mob instanceof TakedaSamuraiEntity || 
                              mob instanceof TakedaSoheiEntity;
            
            boolean isKobayakawa = mob instanceof KobayakawaAshigaruEntity || 
                                  mob instanceof KobayakawaSamuraiEntity || 
                                  mob instanceof KobayakawaSoheiEntity;
            
            // Create banner with appropriate color based on clan
            ItemStack customBanner;
            if (isSatomi) {
                customBanner = new ItemStack(Items.RED_BANNER);
            } else if (isTakeda) {
                customBanner = new ItemStack(Items.WHITE_BANNER);
            } else if (isKobayakawa) {
                customBanner = new ItemStack(Items.BLUE_BANNER);
            } else {
                customBanner = new ItemStack(Items.BLACK_BANNER);
            }
            
            // Build the banner patterns if we have access to registry
            if (mob.level() != null && mob.level().registryAccess() != null) {
                BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();
                
                if (isSatomi) {
                    // Satomi clan: Red banner with white piglin pattern
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.PIGLIN),
                        DyeColor.WHITE
                    );
                } else if (isTakeda) {
                    // Takeda clan: White banner with black skull
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.SKULL),
                        DyeColor.BLACK
                    );
                } else if (isKobayakawa) {
                    // Kobayakawa clan: Blue banner with white mojang
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.MOJANG),
                        DyeColor.WHITE
                    );
                } else {
                    // Default banner: black with custom patterns
                    // Add patterns in order: field masoned (bricks), paly (half vertical), bordure (border), chief (top stripe), skull, roundel (circle middle)
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.BRICKS),
                        DyeColor.WHITE
                    );
                    
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.STRIPE_SMALL),
                        DyeColor.BLACK
                    );
                    
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.BORDER),
                        DyeColor.BLACK
                    );
                    
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.STRIPE_TOP),
                        DyeColor.BLACK
                    );
                    
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.SKULL),
                        DyeColor.YELLOW
                    );
                    
                    builder.add(
                        mob.level().registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN)
                            .getHolderOrThrow(BannerPatterns.CIRCLE_MIDDLE),
                        DyeColor.BLACK
                    );
                }
                
                customBanner.set(DataComponents.BANNER_PATTERNS, builder.build());
            }
            
            // Call setItemSlot again with custom banner
            mob.setItemSlot(slot, customBanner);
            
            // Reset flag
            sengoku$isReplacingBanner = false;
        }
    }
}
