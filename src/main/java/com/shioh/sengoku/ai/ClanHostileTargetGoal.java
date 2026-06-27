package com.shioh.sengoku.ai;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Target goal for clan mobs: becomes hostile to a player if:
 * 1. The player attacks the mob (handled elsewhere)
 * 2. The player has killed a member of this clan (advancement)
 *
 * The clan type and achievement ID are specified during construction.
 */
public class ClanHostileTargetGoal extends TargetGoal {
    private final Mob mob;
    private final String clanName; // "takeda", "kobayakawa", "satomi"
    private final ResourceLocation achievementId;
    private final TagKey<Structure> structureTag;
    private Player candidate;

    public ClanHostileTargetGoal(Mob mob, String clanName) {
        super(mob, false, true);
        this.mob = mob;
        this.clanName = clanName;
        // Advancements live at data/shioh/advancement/main/kill_<clan>_sohei.json
        this.achievementId = ResourceLocation.fromNamespaceAndPath("shioh", "main/kill_" + clanName + "_sohei");
        this.structureTag = TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("minecraft", "samurai_will_guard"));
    }

    @Override
    public boolean canUse() {
        // Keep existing target if valid
        var current = this.mob.getTarget();
        if (current != null && current.isAlive()) return false;

        // Scan for the nearest player who actually has this clan's advancement and is detectable
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (Double.isNaN(followRange) || followRange <= 0) followRange = 24.0;
        double searchRange = Math.min(48.0, followRange + 8.0);

        TargetingConditions conditions = TargetingConditions.forCombat()
            .range(searchRange)
            .selector(entity -> {
                if (!(entity instanceof Player p)) return false;
                if (p.isCreative() || p.isSpectator()) return false;
                // Player must have killed clan OR be in guarded structure
                if (!playerHasKilledClan(p) && !playerInClanGuardStructure(p)) return false;
                return this.mob.getSensing().hasLineOfSight(p);
            });

        this.candidate = this.mob.level().getNearestPlayer(conditions, this.mob);
        return this.candidate != null;
    }

    @Override
    public void start() {
        if (this.candidate != null) {
            this.mob.setTarget(this.candidate);
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        net.minecraft.world.entity.LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!(target instanceof Player player)) {
            return false;
        }
        // Continue only if still a valid, visible, advancement-holding OR structure-present player
        if (player.isCreative() || player.isSpectator()) return false;
        if (!playerHasKilledClan(player) && !playerInClanGuardStructure(player)) return false;
        // Allow pursuit even if temporarily not visible, but drop if far beyond follow range
        double followRange = this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (Double.isNaN(followRange) || followRange <= 0) followRange = 24.0;
        if (this.mob.distanceToSqr(player) > (followRange + 12.0) * (followRange + 12.0)) return false;
        return true;
    }

    /**
     * Check if the player has the "kill_X_sohei" achievement for this clan.
     */
    private boolean playerHasKilledClan(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        
        try {
            // Simply try to find the advancement in the registry
            // If the advancement hasn't been defined yet, return false
            var server = serverPlayer.getServer();
            if (server == null) {
                return false;
            }
            
            // Try looking it up from the advancement registry using a direct approach
            var advancementRegistry = server.getAdvancements();
            
            // Iterate through all advancements to find a match
            // This is a safer approach than using internal APIs
            for (var holder : advancementRegistry.getAllAdvancements()) {
                if (holder.id().equals(achievementId)) {
                    return serverPlayer.getAdvancements().getOrStartProgress(holder).isDone();
                }
            }
        } catch (Exception e) {
            // If anything goes wrong, default to false
        }
        return false;
    }

    /**
     * Check if the player is currently in a samurai_will_guard structure.
     */
    private boolean playerInClanGuardStructure(Player player) {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        try {
            var structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager structureManager = serverLevel.structureManager();

            // Check all structures in the samurai_will_guard tag
            for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(this.structureTag)) {
                Structure structure = holder.value();
                StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                
                // Must be valid structure AND player must be inside its bounding box
                if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                    return true;
                }
            }
        } catch (Throwable e) {
            // Fail-safe: if structure check throws, assume not in structure
            return false;
        }

        return false;
    }
}
