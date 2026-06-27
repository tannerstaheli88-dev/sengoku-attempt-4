package com.shioh.sengoku.mixin;

import com.shioh.sengoku.block.ShideBlock;
import com.shioh.sengoku.registry.SengokuBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to allow rabbit's foot to place shide blocks on walls.
 */
@Mixin(Item.class)
public class RabbitFootPlacementMixin {
    
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        // Cast to get the actual item instance
        Item thisItem = (Item) (Object) this;
        
        // Only handle rabbit's foot or wheat (straw ornament)
        if (thisItem != Items.RABBIT_FOOT && thisItem != Items.WHEAT) {
            return;
        }
        
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Player player = context.getPlayer();
        
        // Only place on horizontal walls
        if (!clickedFace.getAxis().isHorizontal()) {
            return;
        }
        
        BlockState clickedState = level.getBlockState(clickedPos);
        
        // Use lenient check - just needs to be solid, not necessarily a full sturdy face
        // This allows placement on trapdoors, fences, etc., like signs
        boolean canAttach = clickedState.isFaceSturdy(level, clickedPos, clickedFace) || 
                           (!clickedState.isAir() && clickedState.isSolid());
        
        if (!canAttach) {
            return;
        }
        
        // Calculate placement position (away from the wall, into the air)
        BlockPos placePos = clickedPos.relative(clickedFace);
        BlockState placeState = level.getBlockState(placePos);
        
        // Check if the position is replaceable (air, water, etc.)
        if (!placeState.isAir() && !placeState.canBeReplaced()) {
            return;
        }
        
        // Choose block based on item: rabbit's foot -> shide, wheat -> straw_ornament
        Block shideBlock = thisItem == Items.RABBIT_FOOT ? SengokuBlocks.SHIDE : SengokuBlocks.STRAW_ORNAMENT;
        if (shideBlock == null) return;
        
        // Create the shide block state
        // The FACING property indicates which direction the shide is facing (pointing away from wall)
        // canSurvive checks for a block in the OPPOSITE direction of FACING
        // So if we click the WEST face, the shide should face WEST (and check for block to the EAST)
        BlockState shideState = shideBlock.defaultBlockState()
                .setValue(ShideBlock.FACING, clickedFace);
        
        // Verify it can survive here
        if (!shideState.canSurvive(level, placePos)) {
            return;
        }
        
        // Place the block (server-side only)
        if (!level.isClientSide) {
            level.setBlock(placePos, shideState, 3);
            level.playSound(null, placePos, 
                    SoundEvents.PAINTING_PLACE, 
                    SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Consume the item in survival mode
            if (player != null && !player.isCreative()) {
                stack.shrink(1);
            }
        }
        
        // Return success and cancel default behavior
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
