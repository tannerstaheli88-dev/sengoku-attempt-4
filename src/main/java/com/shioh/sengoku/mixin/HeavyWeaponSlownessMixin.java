package com.shioh.sengoku.mixin;

import com.shioh.sengoku.item.KanaboItem;
import com.shioh.sengoku.item.OdachiItem;
import com.shioh.sengoku.item.TetsuboItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class HeavyWeaponSlownessMixin {

    @Unique
    private static final ResourceLocation HEAVY_WEAPON_MODIFIER_ID = 
        ResourceLocation.fromNamespaceAndPath("sengoku", "heavy_weapon_slowness");
    
    @Unique
    private static final double SPEED_REDUCTION = -0.15; // 15% speed reduction (multiply by base speed)
    
    @Unique
    private boolean sengoku$wasHoldingHeavyWeapon = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void applyHeavyWeaponSlowness(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        
        // Check if mob is holding a heavy weapon in main hand
        ItemStack mainHandStack = mob.getMainHandItem();
        boolean isHoldingHeavyWeapon = sengoku$isHeavyWeapon(mainHandStack);
        
        AttributeInstance movementSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;
        
        // Get existing modifier
        AttributeModifier existingModifier = movementSpeed.getModifier(HEAVY_WEAPON_MODIFIER_ID);
        
        if (isHoldingHeavyWeapon) {
            // Apply slowness if not already applied
            if (existingModifier == null) {
                AttributeModifier slowness = new AttributeModifier(
                    HEAVY_WEAPON_MODIFIER_ID,
                    SPEED_REDUCTION,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                movementSpeed.addTransientModifier(slowness);
            }
            sengoku$wasHoldingHeavyWeapon = true;
        } else if (sengoku$wasHoldingHeavyWeapon) {
            // Remove slowness if it was previously applied
            if (existingModifier != null) {
                movementSpeed.removeModifier(HEAVY_WEAPON_MODIFIER_ID);
            }
            sengoku$wasHoldingHeavyWeapon = false;
        }
    }
    
    @Unique
    private boolean sengoku$isHeavyWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        Item item = stack.getItem();
        // Check if the item is a heavy weapon (Kanabo, Tetsubo, or Odachi)
        return item instanceof KanaboItem || 
               item instanceof TetsuboItem || 
               item instanceof OdachiItem;
    }
}
