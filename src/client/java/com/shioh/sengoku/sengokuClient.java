package com.shioh.sengoku;

import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.ModDataComponents;
import com.shioh.sengoku.registry.WeaponRegistry;
import com.shioh.sengoku.registry.ModEntities;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import com.shioh.sengoku.client.model.KunaiModel;
import com.shioh.sengoku.client.model.MacaqueModel;
import com.shioh.sengoku.client.model.OmukadeEndModel;
import com.shioh.sengoku.client.model.OmukadeModel;
import com.shioh.sengoku.client.model.OmukadePartModel;
import com.shioh.sengoku.client.model.IkuchiModel;
import com.shioh.sengoku.client.model.IkuchiPartModel;
import com.shioh.sengoku.client.model.IkuchiEndModel;
import com.shioh.sengoku.client.model.UmiBozuModel;
import com.shioh.sengoku.client.model.YukiOnnaModel;
import com.shioh.sengoku.client.model.OnikumaModel;
import com.shioh.sengoku.client.model.SarugamiModel;
import com.shioh.sengoku.client.model.HitotsumeNyudoModel;
import com.shioh.sengoku.client.model.UmiNyoboModel;
import com.shioh.sengoku.client.model.UmiInuModel;
import com.shioh.sengoku.client.model.DragonPartModel;
import com.shioh.sengoku.client.model.DragonPartEndModel;
import com.shioh.sengoku.client.model.DragonNeckModel;
import com.shioh.sengoku.client.model.DragonArmsModel;
import com.shioh.sengoku.client.model.DragonHeadModel;
import com.shioh.sengoku.client.model.DragonPartThinModel;
import com.shioh.sengoku.client.model.DragonTailModel;
import com.shioh.sengoku.client.renderer.BanditRenderer;
import com.shioh.sengoku.client.renderer.MacaqueRenderer;
import com.shioh.sengoku.client.renderer.RoninRenderer;
import com.shioh.sengoku.client.renderer.KunaiRenderer;
import com.shioh.sengoku.client.renderer.OniBruteRenderer;
import com.shioh.sengoku.client.renderer.UmiNyoboRenderer;
import com.shioh.sengoku.client.renderer.UmiInuRenderer;
import com.shioh.sengoku.client.renderer.IkuchiRenderer;
import com.shioh.sengoku.client.renderer.IkuchiPartRenderer;
import com.shioh.sengoku.client.renderer.IkuchiEndRenderer;
import com.shioh.sengoku.client.renderer.UmiBozuRenderer;
import com.shioh.sengoku.client.renderer.OmukadeRenderer;
import com.shioh.sengoku.client.renderer.OmukadePartRenderer;
import com.shioh.sengoku.client.renderer.OmukadeEndRenderer;
import com.shioh.sengoku.client.renderer.DragonPartRenderer;
import com.shioh.sengoku.client.renderer.DragonPartEndRenderer;
import com.shioh.sengoku.client.renderer.DragonNeckRenderer;
import com.shioh.sengoku.client.renderer.DragonArmsRenderer;
import com.shioh.sengoku.client.renderer.DragonHeadRenderer;
import com.shioh.sengoku.client.renderer.DragonPartThinRenderer;
import com.shioh.sengoku.client.renderer.DragonTailRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import com.shioh.sengoku.struct.WeaponType;
import com.shioh.sengoku.client.BlockClashParticle;
import com.shioh.sengoku.sengokuFabric; // gives access to MODID and helpers
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
// Render-layer registration is done reflectively below to remain compatible with multiple Fabric API versions.
import net.minecraft.client.gui.screens.MenuScreens;
import com.shioh.sengoku.init.KenseiItemReg;
import com.shioh.sengoku.init.HatItemReg;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.client.particle.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.renderer.item.ItemProperties;
import com.shioh.sengoku.util.BedShapeState;
import net.fabricmc.fabric.api.resource.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.shioh.sengoku.client.MusicController;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.MaceItem;
import com.shioh.sengoku.config.PostureValues;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

public class sengokuClient implements ClientModInitializer {
    
    private static final ResourceLocation BLOCKING = ResourceLocation.withDefaultNamespace("blocking");
    private static final ResourceLocation YUKI_ONNA_VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    // Stealth vignette texture for concealment (uses mod asset)
    private static final ResourceLocation STEALTH_VIGNETTE = sengokuFabric.asId("textures/misc/stealth_vignette.png");
    private static boolean stealthVignetteLogged = false;

    // Convenience helper: render a full-screen textured overlay similar to powder snow overlay.
    // Parameters: texture, tint RGB, and alpha (0.0-1.0). This centralizes render state
    // so the vignette behavior is easy to tweak in one place.
    private static void renderFullScreenOverlay(GuiGraphics guiGraphics, ResourceLocation texture, float r, float g, float b, float alpha) {
        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(r, g, b, alpha);

        // If using the stealth vignette texture, log once so we can confirm it's active
        try {
            if (texture.equals(STEALTH_VIGNETTE) && !stealthVignetteLogged) {
                stealthVignetteLogged = true;
                try { sengokuFabric.LOGGER.info("[sengoku] Using STEALTH_VIGNETTE for conceal overlay"); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        guiGraphics.blit(texture, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    private static float yukiOnnaStalkOverlayStrength = 0.0F;
    private static boolean yukiOnnaStalkActive = false;
    // Concealment vignette
    private static float concealVignetteStrength = 0.0F;
    private static boolean concealVignetteActive = false;
    private static float playerPoiseCurrent = 0.0F;
    private static float playerPoiseMax = 20.0F;
    // Debug HUD
    private static final java.util.List<String> DEBUG_LINES = new java.util.ArrayList<>();
    private static boolean DEBUG_ENABLED = false;
    private static final boolean SHOW_POISE_WHEN_DAMAGED_ONLY = true;
    // Sampled entity-load cache used to scale mist weather volumetric fog spawns.
    private static int cachedMistRenderableEntityCount = 0;
    private static int cachedMistRenderableEntitySampleTick = -1000;
    // Motion tracking for mist weather volumetric fog so the cloud keeps up with player movement.
    private static double mistLastPlayerX = Double.NaN;
    private static double mistLastPlayerZ = Double.NaN;

    private static void registerWeaponPostureTooltips() {
        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            int postureDamage = 0;

            Integer componentValue = stack.get(ModDataComponents.WEAPON_POSTURE_DAMAGE);
            if (componentValue != null && componentValue > 0) {
                postureDamage = componentValue;
            } else if (
                (stack.getItem() instanceof SwordItem) ||
                (stack.getItem() instanceof AxeItem) ||
                (stack.getItem() instanceof TridentItem) ||
                (stack.getItem() instanceof MaceItem)
            ) {
                String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
                if ("minecraft".equals(namespace)) {
                    postureDamage = getVanillaWeaponPostureDamage(stack);
                }
            }

            if (postureDamage > 0) {
                tooltip.add(Component.translatable("item.sengoku.posture_damage", postureDamage).withStyle(ChatFormatting.GREEN));
            }
        });
    }

    private static int getVanillaWeaponPostureDamage(ItemStack stack) {
        if (stack.getItem() instanceof TridentItem) {
            return PostureValues.VANILLA_TRIDENT;
        }

        if (stack.getItem() instanceof MaceItem) {
            return PostureValues.VANILLA_MACE;
        }

        if (!(stack.getItem() instanceof TieredItem tieredItem)) {
            return 0;
        }

        int base = getTierBasePosture(tieredItem.getTier());
        if (base <= 0) {
            return 0;
        }

        if (stack.getItem() instanceof AxeItem) {
            return base + PostureValues.VANILLA_AXE_BONUS;
        }

        return base;
    }

    private static int getTierBasePosture(Tier tier) {
        if (tier == net.minecraft.world.item.Tiers.WOOD || tier == net.minecraft.world.item.Tiers.GOLD) return PostureValues.VANILLA_WOOD_GOLD_BASE;
        if (tier == net.minecraft.world.item.Tiers.STONE) return PostureValues.VANILLA_STONE_BASE;
        if (tier == net.minecraft.world.item.Tiers.IRON) return PostureValues.VANILLA_IRON_BASE;
        if (tier == net.minecraft.world.item.Tiers.DIAMOND) return PostureValues.VANILLA_DIAMOND_BASE;
        if (tier == net.minecraft.world.item.Tiers.NETHERITE) return PostureValues.VANILLA_NETHERITE_BASE;
        return 0;
    }
    

    @Override
    public void onInitializeClient() {
        registerWeaponPostureTooltips();

        // Register payload codec on client for S2C
        try {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.VillageMusicPayload.TYPE, com.shioh.sengoku.network.VillageMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.RaidMusicPayload.TYPE, com.shioh.sengoku.network.RaidMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.YukiOnnaMusicPayload.TYPE, com.shioh.sengoku.network.YukiOnnaMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CastleMusicPayload.TYPE, com.shioh.sengoku.network.CastleMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.ShinobiMusicPayload.TYPE, com.shioh.sengoku.network.ShinobiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.ShinobiLordMusicPayload.TYPE, com.shioh.sengoku.network.ShinobiLordMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.WarlordMusicPayload.TYPE, com.shioh.sengoku.network.WarlordMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CastleCombatMusicPayload.TYPE, com.shioh.sengoku.network.CastleCombatMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatBasicMusicPayload.TYPE, com.shioh.sengoku.network.CombatBasicMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatYomiMusicPayload.TYPE, com.shioh.sengoku.network.CombatYomiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatRyuguMusicPayload.TYPE, com.shioh.sengoku.network.CombatRyuguMusicPayload.CODEC);
            
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.TatarigamiMusicPayload.TYPE, com.shioh.sengoku.network.TatarigamiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.PlayerPoisePayload.TYPE, com.shioh.sengoku.network.PlayerPoisePayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.DebugTogglePayload.TYPE, com.shioh.sengoku.network.DebugTogglePayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.DebugDataPayload.TYPE, com.shioh.sengoku.network.DebugDataPayload.CODEC);
        } catch (Throwable ignored) {}

        // Register client receivers for debug HUD
        try {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.DebugTogglePayload.TYPE,
                (payload, context) -> context.client().execute(() -> DEBUG_ENABLED = payload.enabled())
            );

            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.DebugDataPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    synchronized (DEBUG_LINES) {
                        DEBUG_LINES.clear();
                        DEBUG_LINES.addAll(payload.lines());
                    }
                })
            );

            HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
                if (!DEBUG_ENABLED) return;
                Minecraft mc = Minecraft.getInstance();
                int left = 14;
                int maxWidth = mc.getWindow().getGuiScaledWidth() - left - 8;
                int y = 10;
                synchronized (DEBUG_LINES) {
                    for (String s : DEBUG_LINES) {
                        for (var line : mc.font.split(net.minecraft.network.chat.Component.literal(s), maxWidth)) {
                            matrices.drawString(mc.font, line, left, y, 0xFFFFFF);
                            y += mc.font.lineHeight + 2;
                        }
                    }
                }
                String musicCategory = com.shioh.sengoku.client.MusicController.getCurrentMusicCategoryLabel();
                matrices.drawString(mc.font, "Music: " + musicCategory, 10, y, 0xFFFFFF);
            });
        } catch (Throwable ignored) {}
        
        // Register model layers
        EntityModelLayerRegistry.registerModelLayer(KunaiModel.LAYER_LOCATION, KunaiModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(MacaqueModel.LAYER_LOCATION, MacaqueModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(YukiOnnaModel.LAYER_LOCATION, YukiOnnaModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(OnikumaModel.LAYER_LOCATION, OnikumaModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SarugamiModel.LAYER_LOCATION, SarugamiModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HitotsumeNyudoModel.LAYER_LOCATION, HitotsumeNyudoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(UmiNyoboModel.LAYER_LOCATION, UmiNyoboModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(UmiInuModel.LAYER_LOCATION, UmiInuModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(IkuchiModel.LAYER_LOCATION, IkuchiModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(IkuchiPartModel.LAYER_LOCATION, IkuchiPartModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(IkuchiEndModel.LAYER_LOCATION, IkuchiEndModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(OmukadeModel.LAYER_LOCATION, OmukadeModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(OmukadePartModel.LAYER_LOCATION, OmukadePartModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(OmukadeEndModel.LAYER_LOCATION, OmukadeEndModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonPartModel.LAYER_LOCATION, DragonPartModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonHeadModel.LAYER_LOCATION, DragonHeadModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonPartThinModel.LAYER_LOCATION, DragonPartThinModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonPartEndModel.LAYER_LOCATION, DragonPartEndModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonNeckModel.LAYER_LOCATION, DragonNeckModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonArmsModel.LAYER_LOCATION, DragonArmsModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DragonTailModel.LAYER_LOCATION, DragonTailModel::createBodyLayer);
        // Simple editable models for Kojin, Ningyo, and Akugyo
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.UmiBozuModel.LAYER_LOCATION, com.shioh.sengoku.client.model.UmiBozuModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.KojinModel.LAYER_LOCATION, com.shioh.sengoku.client.model.KojinModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.NingyoModel.LAYER_LOCATION, com.shioh.sengoku.client.model.NingyoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.AkugyoModel.LAYER_LOCATION, com.shioh.sengoku.client.model.AkugyoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.OniBruteModel.LAYER_LOCATION, com.shioh.sengoku.client.model.OniBruteModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.RedCrownedCraneModel.LAYER_LOCATION, com.shioh.sengoku.client.model.RedCrownedCraneModel::createBodyLayer);
        // Maikubi model layer (custom Wither-like enemy)
        EntityModelLayerRegistry.registerModelLayer(com.shioh.sengoku.client.model.MaikubiModel.LAYER_LOCATION, com.shioh.sengoku.client.model.MaikubiModel::createBodyLayer);
        
        // Register entity renderers
        EntityRendererRegistry.register(ModEntities.KUNAI, KunaiRenderer::new);
        EntityRendererRegistry.register(ModEntities.IKUCHI, IkuchiRenderer::new);
        EntityRendererRegistry.register(ModEntities.IKUCHI_PART, IkuchiPartRenderer::new);
        EntityRendererRegistry.register(ModEntities.IKUCHI_END, IkuchiEndRenderer::new);
        EntityRendererRegistry.register(ModEntities.UMI_BOZU, com.shioh.sengoku.client.renderer.UmiBozuRenderer::new);
        EntityRendererRegistry.register(ModEntities.BANDIT, BanditRenderer::new);
        EntityRendererRegistry.register(ModEntities.RONIN, RoninRenderer::new);
        EntityRendererRegistry.register(ModEntities.GORYO, com.shioh.sengoku.client.renderer.GoryoRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHIRYO, com.shioh.sengoku.client.renderer.ShiryoRenderer::new);
        EntityRendererRegistry.register(ModEntities.UMI_NYOBO, UmiNyoboRenderer::new);
        EntityRendererRegistry.register(ModEntities.UMI_INU, UmiInuRenderer::new);
        EntityRendererRegistry.register(ModEntities.MAIKUBI, com.shioh.sengoku.client.renderer.MaikubiRenderer::new);
        // Warlord boss renderer (uses illager model with enraged texture on low HP)
        EntityRendererRegistry.register(ModEntities.WARLORD, com.shioh.sengoku.client.renderer.WarlordRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHINOBI_LORD, com.shioh.sengoku.client.renderer.ShinobiLordRenderer::new);
        EntityRendererRegistry.register(ModEntities.YUKI_ONNA, com.shioh.sengoku.client.renderer.YukiOnnaRenderer::new);
        
        // Register clan entity renderers
        EntityRendererRegistry.register(ModEntities.KOBAYAKAWA_ASHIGARU, com.shioh.sengoku.client.renderer.KobayakawaAshigaruRenderer::new);
        EntityRendererRegistry.register(ModEntities.KOBAYAKAWA_SAMURAI, com.shioh.sengoku.client.renderer.KobayakawaSamuraiRenderer::new);
        EntityRendererRegistry.register(ModEntities.KOBAYAKAWA_SOHEI, com.shioh.sengoku.client.renderer.KobayakawaSoheiRenderer::new);
        EntityRendererRegistry.register(ModEntities.TAKEDA_ASHIGARU, com.shioh.sengoku.client.renderer.TakedaAshigaruRenderer::new);
        EntityRendererRegistry.register(ModEntities.TAKEDA_SAMURAI, com.shioh.sengoku.client.renderer.TakedaSamuraiRenderer::new);
        EntityRendererRegistry.register(ModEntities.TAKEDA_SOHEI, com.shioh.sengoku.client.renderer.TakedaSoheiRenderer::new);
        EntityRendererRegistry.register(ModEntities.SATOMI_ASHIGARU, com.shioh.sengoku.client.renderer.SatomiAshigaruRenderer::new);
        EntityRendererRegistry.register(ModEntities.SATOMI_SAMURAI, com.shioh.sengoku.client.renderer.SatomiSamuraiRenderer::new);
        EntityRendererRegistry.register(ModEntities.SATOMI_SOHEI, com.shioh.sengoku.client.renderer.SatomiSoheiRenderer::new);
        
        // Register yokai entity renderers
        EntityRendererRegistry.register(ModEntities.ONIKUMA, com.shioh.sengoku.client.renderer.OnikumaRenderer::new);
            EntityRendererRegistry.register(ModEntities.GAKI, com.shioh.sengoku.client.renderer.GakiRenderer::new);
            EntityRendererRegistry.register(ModEntities.KOJIN, com.shioh.sengoku.client.renderer.KojinRenderer::new);
            EntityRendererRegistry.register(ModEntities.NINGYO, com.shioh.sengoku.client.renderer.NingyoRenderer::new);
            EntityRendererRegistry.register(ModEntities.KAMIIKE_HIME, com.shioh.sengoku.client.renderer.KamiikeHimeRenderer::new);
            EntityRendererRegistry.register(ModEntities.AKUGYO, com.shioh.sengoku.client.renderer.AkugyoRenderer::new);
        EntityRendererRegistry.register(ModEntities.ONI_BRUTE, OniBruteRenderer::new);
        EntityRendererRegistry.register(ModEntities.SARUGAMI, com.shioh.sengoku.client.renderer.SarugamiRenderer::new);
        EntityRendererRegistry.register(ModEntities.HITOTSUME_NYUDO, com.shioh.sengoku.client.renderer.HitotsumeNyudoRenderer::new);
        EntityRendererRegistry.register(ModEntities.RED_CROWNED_CRANE, com.shioh.sengoku.client.renderer.RedCrownedCraneRenderer::new);
        // Macaque renderer
        EntityRendererRegistry.register(ModEntities.MACAQUE, MacaqueRenderer::new);
        // Crow renderer (uses parrot model)
        EntityRendererRegistry.register(ModEntities.CROW, com.shioh.sengoku.client.renderer.CrowRenderer::new);
        // Omukade renderers
        EntityRendererRegistry.register(ModEntities.OMUKADE, OmukadeRenderer::new);
        EntityRendererRegistry.register(ModEntities.OMUKADE_PART, OmukadePartRenderer::new);
        EntityRendererRegistry.register(ModEntities.OMUKADE_END, OmukadeEndRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_PART, DragonPartRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_HEAD, DragonHeadRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_PART_THIN, DragonPartThinRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_PART_END, DragonPartEndRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_NECK, DragonNeckRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_ARMS, DragonArmsRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRAGON_TAIL, DragonTailRenderer::new);

        if (FabricLoader.getInstance().isModLoaded("enhancedblockentities")) {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    sengokuFabric.asId("enhanced-beds-lighting-fix"),
                    FabricLoader.getInstance().getModContainer(sengokuFabric.MODID).orElseThrow(),
                    Component.translatable("resourcePack.sengoku.enhanced-beds-lighting-fix.name"),
                    ResourcePackActivationType.ALWAYS_ENABLED);
        }
            ResourceManagerHelper.registerBuiltinResourcePack(
                    sengokuFabric.asId("more-pillowed-bed-variants"),
                    FabricLoader.getInstance().getModContainer(sengokuFabric.MODID).orElseThrow(),
                    Component.translatable("resourcePack.sengoku.more-pillowed-bed-variants.name"),
                    ResourcePackActivationType.NORMAL);
            ResourceManagerHelper.registerBuiltinResourcePack(
                    sengokuFabric.asId("more-pillowed-connected-bed-variants"),
                    FabricLoader.getInstance().getModContainer(sengokuFabric.MODID).orElseThrow(),
                    Component.translatable("resourcePack.sengoku.more-pillowed-connected-bed-variants.name"),
                    ResourcePackActivationType.NORMAL);
                ResourceManagerHelper.registerBuiltinResourcePack(
                    sengokuFabric.asId("for_sodium_users"),
                    FabricLoader.getInstance().getModContainer(sengokuFabric.MODID).orElseThrow(),
                    Component.literal("For Sodium Users"),
                    ResourcePackActivationType.NORMAL);
                ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            new IdentifiableResourceReloadListener() {
                final ResourceLocation mBedVListener = sengokuFabric.asId("resource_reload_listener");
                @Override
                public ResourceLocation getFabricId() {
                    return mBedVListener;
                }

                @Override
                public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                    return CompletableFuture.runAsync(() -> {}, backgroundExecutor).thenCompose(preparationBarrier::wait).thenRunAsync(() ->
                    {
                     BedShapeState.needsToBeChecked = true;
                    }, gameExecutor);
                }

                @Override
                public @NotNull String getName() {
                    return mBedVListener.toString();
                }
        });
    
        // ✅ Register spark particles
        ParticleFactoryRegistry.getInstance().register(
            ParticleRegistry.POSTURE_SPARK,
            SparkParticleProvider::new
        );

        // ✅ Register block clash particles
        ParticleFactoryRegistry.getInstance().register(
            ParticleRegistry.BLOCK_PARTICLE,
            BlockClashParticle.Provider::new
        );

        // ✅ Register fog particles for warm water
        ParticleFactoryRegistry.getInstance().register(
            ParticleRegistry.FOG,
            FogParticleProvider::new
        );

        // ✅ Register mist weather fog particles (no dithering)
        ParticleFactoryRegistry.getInstance().register(
            ParticleRegistry.FOG_MIST,
            MistWeatherFogParticleProvider::new
        );

        // ✅ Register flat fog particles for warm water surface
        ParticleFactoryRegistry.getInstance().register(
            ParticleRegistry.FOG_FLAT,
            FogFlatParticleProvider::new
        );

        // ✅ Register custom musket smoke particles
        ParticleFactoryRegistry.getInstance().register(
            com.shioh.sengoku.particle.ModParticles.MUSKET_SMOKE,
            com.shioh.sengoku.particle.MusketSmokeParticle.Provider::new
        );

        // ✅ Register dragon_splash particle (overlay for dragon breath)
        try {
            ParticleFactoryRegistry.getInstance().register(
                com.shioh.sengoku.particle.ModParticles.DRAGON_SPLASH,
                com.shioh.sengoku.particle.DragonSplashParticle.Provider::new
            );
            sengokuFabric.LOGGER.info("Registered DRAGON_SPLASH particle factory");
        } catch (Throwable ignored) {}

        // ✅ Register custom gunfire flash particles
        ParticleFactoryRegistry.getInstance().register(
            com.shioh.sengoku.particle.ModParticles.GUNFIRE_FLASH,
            com.shioh.sengoku.particle.GunfireFlashParticle.Provider::new
        );

            // ✅ Register blood particles
            ParticleFactoryRegistry.getInstance().register(
                ParticleRegistry.BLOOD_PARTICLE,
                com.shioh.sengoku.particle.BloodParticle.Provider::new
            );

            // ✅ Register detection particle (used when mobs detect players)
            try {
                ParticleFactoryRegistry.getInstance().register(
                    ParticleRegistry.DETECTION_PARTICLE,
                    DetectionParticle.Provider::new
                );
            } catch (Throwable ignored) {}

            // ✅ Register flowing leaves particle (fast fixed wind direction)
            ParticleFactoryRegistry.getInstance().register(
                ParticleRegistry.FLOWING_LEAVES,
                com.shioh.sengoku.particle.FlowingLeavesParticle.Provider::new
            );

    // Sengoku particles registered (debug print removed)

        // ✅ Blocking property for vanilla swords
        registerBlocking(Items.DIAMOND_SWORD);
        registerBlocking(Items.NETHERITE_SWORD);
        registerBlocking(Items.STONE_SWORD);
        registerBlocking(Items.WOODEN_SWORD);
        registerBlocking(Items.IRON_SWORD);
        registerBlocking(Items.GOLDEN_SWORD);
        registerBlocking(Items.MACE);
        registerBlocking(Items.DIAMOND_AXE);
        registerBlocking(Items.NETHERITE_AXE);
        registerBlocking(Items.STONE_AXE);
        registerBlocking(Items.WOODEN_AXE);
        registerBlocking(Items.IRON_AXE);
        registerBlocking(Items.GOLDEN_AXE);
        registerBlocking(Items.DIAMOND_HOE);
        registerBlocking(Items.NETHERITE_HOE);
        registerBlocking(Items.STONE_HOE);
        registerBlocking(Items.WOODEN_HOE);
        registerBlocking(Items.IRON_HOE);
        registerBlocking(Items.GOLDEN_HOE);

        // ✅ Blocking property for custom weapons
        for (Item odachi : WeaponRegistry.getItemsByType(WeaponType.ODACHI)) registerBlocking(odachi);
        for (Item yari : WeaponRegistry.getItemsByType(WeaponType.YARI)) registerBlocking(yari);
        for (Item naginata : WeaponRegistry.getItemsByType(WeaponType.NAGINATA)) registerBlocking(naginata);
        for (Item tanto : WeaponRegistry.getItemsByType(WeaponType.TANTO)) registerBlocking(tanto);
        for (Item kanabo : WeaponRegistry.getItemsByType(WeaponType.KANABO)) registerBlocking(kanabo);
        for (Item tetsubo : WeaponRegistry.getItemsByType(WeaponType.TETSUBO)) registerBlocking(tetsubo);
    // ✅ Blocking property for Blade of the Kensei (custom item not part of WeaponRegistry)
    registerBlocking(KenseiItemReg.BLADE_OF_THE_KENSEI);
    registerBlocking(KenseiItemReg.NETHERITE_BLADE_OF_THE_KENSEI);
    registerBlocking(KenseiItemReg.SPLITTING_AXE_OF_KINTARO);
    registerBlocking(KenseiItemReg.NETHERITE_SPLITTING_AXE_OF_KINTARO);
    // ✅ Blocking property for legendary weapons
    registerBlocking(KenseiItemReg.NAGINATA_OF_THE_NOBUSHI);
    registerBlocking(KenseiItemReg.NETHERITE_NAGINATA_OF_THE_NOBUSHI);
    registerBlocking(KenseiItemReg.KANABO_OF_OTAKEMARU);
    registerBlocking(KenseiItemReg.NETHERITE_KANABO_OF_OTAKEMARU);
    registerBlocking(KenseiItemReg.TETSUBO_OF_THE_HATAMOTO);
    registerBlocking(KenseiItemReg.NETHERITE_TETSUBO_OF_THE_HATAMOTO);
    registerBlocking(KenseiItemReg.ODACHI_OF_THE_SHUGODAI);
    registerBlocking(KenseiItemReg.NETHERITE_ODACHI_OF_THE_SHUGODAI);
    registerBlocking(KenseiItemReg.YARI_OF_THE_TAISHO);
    registerBlocking(KenseiItemReg.NETHERITE_YARI_OF_THE_TAISHO);
    registerBlocking(KenseiItemReg.TANTO_OF_TAMATORI_HIME);
    registerBlocking(KenseiItemReg.NETHERITE_TANTO_OF_TAMATORI_HIME);

        // Register custom screen for brewing menus so our Sake Brewery uses its own GUI
        try {
            // Register our client screen for the SakeBrewery menu type with explicit generic types
            // Register Sake Brewery screen
            MenuScreens.register(com.shioh.sengoku.registry.ModMenuTypes.SAKE_BREWERY, com.shioh.sengoku.screen.SakeBreweryScreen::new);
            // Register dedicated Boiling Pot screen (separate texture)
            MenuScreens.register(com.shioh.sengoku.registry.ModMenuTypes.BOILING_POT,
                new MenuScreens.ScreenConstructor<com.shioh.sengoku.screen.BoilingPotMenu, net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<com.shioh.sengoku.screen.BoilingPotMenu>>() {
                    @Override
                    public net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<com.shioh.sengoku.screen.BoilingPotMenu> create(com.shioh.sengoku.screen.BoilingPotMenu menu, net.minecraft.world.entity.player.Inventory inv, net.minecraft.network.chat.Component title) {
                        try {
                            return new com.shioh.sengoku.screen.BoilingPotScreen(menu, inv, title);
                        } catch (Throwable t) {
                            sengokuFabric.LOGGER.warn("BoilingPotScreen construction failed, falling back to minimal safe screen", t);
                            return new com.shioh.sengoku.screen.BoilingPotScreenFallback(menu, inv, title);
                        }
                    }
                }
            );
        } catch (Throwable t) {
            // Failed to register SakeBreweryScreen - ignore on client to avoid noisy logs
        }

        // Make shoji frames translucent like stained glass (exclude covered shoji frames)
        // Use reflection so compilation doesn't require a specific Fabric API client-rendering class.
        try {
            Class<?> renderLayerMapClass = null;
            try {
                renderLayerMapClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.RenderLayerMap");
            } catch (ClassNotFoundException e) {
                try {
                    renderLayerMapClass = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap");
                } catch (ClassNotFoundException e2) {
                    renderLayerMapClass = null;
                }
            }

            if (renderLayerMapClass != null) {
                Class<?> renderTypeClass = Class.forName("net.minecraft.client.renderer.RenderType");
                Object translucent = renderTypeClass.getMethod("translucent").invoke(null);
                Object cutout = renderTypeClass.getMethod("cutout").invoke(null);

                // Register crop blocks as cutout for transparency
                try {
                    Object instance = null;
                    try {
                        instance = renderLayerMapClass.getField("INSTANCE").get(null);
                    } catch (NoSuchFieldException ignored) {}

                    for (java.lang.reflect.Method m : renderLayerMapClass.getMethods()) {
                        if (!m.getName().equals("putBlocks") && !m.getName().equals("putBlock")) continue;
                        Class<?>[] params = m.getParameterTypes();
                        if (params.length == 2) {
                            try {
                                if (m.getName().equals("putBlocks")) {
                                    m.invoke(instance, cutout, 
                                        com.shioh.sengoku.init.TansuBlockReg.TEA_CROP,
                                        com.shioh.sengoku.init.TansuBlockReg.RICE_CROP,
                                        com.shioh.sengoku.init.TansuBlockReg.RAMIE_CROP,
                                        com.shioh.sengoku.registry.SengokuBlocks.PALE_REEDS_BLOCK,
                                        com.shioh.sengoku.registry.SengokuBlocks.DARK_REEDS_BLOCK,
                                        com.shioh.sengoku.registry.SengokuBlocks.SHIDE,
                                        com.shioh.sengoku.init.TansuBlockReg.FISHING_NET);
                                } else {
                                    m.invoke(null, com.shioh.sengoku.init.TansuBlockReg.TEA_CROP, cutout);
                                    m.invoke(null, com.shioh.sengoku.init.TansuBlockReg.RICE_CROP, cutout);
                                    m.invoke(null, com.shioh.sengoku.init.TansuBlockReg.RAMIE_CROP, cutout);
                                }
                                break;
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}

                for (var entry : com.shioh.sengoku.registry.SengokuBlocks.SHOJI_FRAMES.entrySet()) {
                    String key = entry.getKey();
                    // only target the non-covered frame variants
                    boolean isFrame = key.endsWith("_shoji_frame") || key.endsWith("_checkered_shoji_frame") || key.endsWith("_paly_shoji_frame");
                    boolean isCovered = key.endsWith("_covered_shoji_frame");
                    if (!isFrame || isCovered) continue;

                    // Try instance method RenderLayerMap.INSTANCE.putBlocks(RenderType, Block...)
                    try {
                        Object instance = null;
                        try {
                            instance = renderLayerMapClass.getField("INSTANCE").get(null);
                        } catch (NoSuchFieldException ignored) {}

                        boolean invoked = false;

                        // Try putBlocks(RenderType, Block...)
                        for (java.lang.reflect.Method m : renderLayerMapClass.getMethods()) {
                            if (!m.getName().equals("putBlocks")) continue;
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 2 && params[0].isAssignableFrom(renderTypeClass)) {
                                m.invoke(instance, translucent, entry.getValue());
                                invoked = true;
                                break;
                            }
                        }

                        if (invoked) continue;

                        // Try putBlock(Block, RenderType)
                        for (java.lang.reflect.Method m : renderLayerMapClass.getMethods()) {
                            if (!m.getName().startsWith("put")) continue;
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 2 && params[0].isAssignableFrom(entry.getValue().getClass()) && params[1].isAssignableFrom(renderTypeClass)) {
                                m.invoke(null, entry.getValue(), translucent);
                                invoked = true;
                                break;
                            }
                        }
                    } catch (Throwable ignored) {
                        // best-effort only
                    }
                }
            }
        } catch (Throwable ignored) {
            // cosmetic only
        }
        
        // Register warm water client tick system (client-side particles like vanilla)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            warmWaterClientTick();
            mistWeatherClientTick();
            yukiOnnaStalkClientTick();
            concealClientTick();
            
            // Music controller - chooses music based on time, depth, and biome
            MusicController.clientTick();
        });

        // Village music sync receiver (payload-based)
        try {
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.VillageMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.inside();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setVillageStructureFlag(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.CastleMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.inside();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setCastleStructureFlag(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.RaidMusicPayload.TYPE,
                (payload, context) -> {
                    final int state = payload.state();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setRaidState(state));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.YukiOnnaMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setYukiOnnaServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.ShinobiMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setShinobiServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.ShinobiLordMusicPayload.TYPE,
                (payload, context) -> {
                    final int phase = payload.phase();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setShinobiLordServerPhase(phase));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.WarlordMusicPayload.TYPE,
                (payload, context) -> {
                    final int phase = payload.phase();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setWarlordServerPhase(phase));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.CastleCombatMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setCastleCombatServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.CombatBasicMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setCombatBasicServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.CombatYomiMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setCombatYomiServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.CombatRyuguMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setCombatRyuguServerActive(flag));
                }
            );
            
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.TatarigamiMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setTatarigamiServerActive(flag));
                }
            );
            // Ender Dragon music is client-driven by the boss overlay; no network receiver required
            // --- Patrol (bandit/clan) ---
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.PatrolMusicPayload.TYPE,
                (payload, context) -> {
                    final boolean flag = payload.active();
                    context.client().execute(() -> com.shioh.sengoku.client.MusicController.setPatrolServerActive(flag));
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.WarmWaterConfigPayload.TYPE,
                (payload, context) -> {
                    final boolean enabled = payload.enabled();
                    context.client().execute(() -> {
                        try {
                            com.shioh.sengoku.config.SengokuConfig.getInstance().warmWaterEnabled = enabled;
                        } catch (Throwable ignored) {}
                    });
                }
            );
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.shioh.sengoku.network.PlayerPoisePayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    playerPoiseCurrent = Math.max(0.0F, payload.current());
                    playerPoiseMax = Math.max(0.001F, payload.max());
                })
            );
            
        } catch (Throwable ignored) {}

        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> renderYukiOnnaStalkOverlay(guiGraphics));
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> renderConcealOverlay(guiGraphics));
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> renderPoiseMeter(guiGraphics));

        // Tint warm water brighter via block color provider (affects rendered water quads)
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            if (view != null && pos != null) {
                try {
                    if (view instanceof Level lvl && isNearMagmaBlock(lvl, pos)) {
                        return getWarmWaterColor();
                    }
                    return BiomeColors.getAverageWaterColor(view, pos);
                } catch (Throwable ignored) {}
            }
            return 0x3F76E4; // vanilla default fallback
        }, Blocks.WATER);

        // Hat tinting removed for debugging; color handling moved out for now.

        // Make warm water feel more opaque by tightening fog when the camera is in it
        WorldRenderEvents.START.register(context -> {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p == null) return;
            
            // Warm water fog (mist weather fog is handled by MistWeatherFogMixin)
            if (p.isInWater() && isPlayerInWarmWater()) {
                // Much shorter fog distance => more opaque, murky hot spring look
                RenderSystem.setShaderFogStart(0.0f);
                RenderSystem.setShaderFogEnd(3.0f); // ~3 blocks visibility - very opaque
                
                // Set fog color to milky blue-white for hot spring effect
                RenderSystem.setShaderFogColor(0.91f, 0.96f, 0.97f); // RGB of #E8F4F8
            }
        });
    }

    private void registerBlocking(Item item) {
        ItemProperties.register(item, BLOCKING,
            (stack, level, entity, seed) ->
                (entity != null && entity.isUsingItem() && entity.getUseItem() == stack) ? 1.0F : 0.0F
        );
    }

    // ============================================================
    // Spark Particle (flying sparks, falls down, small + quick)
    // ============================================================
    public static class GlowParticle extends TextureSheetParticle {
        private final SpriteSet sprites;

        protected GlowParticle(ClientLevel world, double x, double y, double z,
                               double velocityX, double velocityY, double velocityZ,
                               SpriteSet spriteSet) {
            super(world, x, y, z, velocityX, velocityY, velocityZ);
            this.sprites = spriteSet;

            this.gravity = 0.4F;
            this.lifetime = 15 + this.random.nextInt(5);
            this.scale(0.3F + this.random.nextFloat() * 0.1F);
            this.setSpriteFromAge(spriteSet);

            this.rCol = 1.0F;
            this.gCol = 0.8F + this.random.nextFloat() * 0.2F;
            this.bCol = 0.3F + this.random.nextFloat() * 0.2F;
        }

        @Override
        public void tick() {
            super.tick();
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_LIT; // Use lit sheet for glowing effect
        }

        @Override
        public int getLightColor(float partialTick) {
            return 0xF000F0; // Max brightness (both sky and block light at 15)
        }
    }

    public static class SparkParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public SparkParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new GlowParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteSet);
        }
    }

    // Minimal detection particle that only moves vertically (no horizontal motion)
    public static class DetectionParticle extends TextureSheetParticle {
        private final SpriteSet sprites;

        protected double vyField;
        protected DetectionParticle(ClientLevel world, double x, double y, double z,
                                    double velocityX, double velocityY, double velocityZ,
                                    SpriteSet spriteSet) {
            // Start with no horizontal velocity; vertical movement will be applied manually
            super(world, x, y, z, 0.0D, 0.0D, 0.0D);
            this.sprites = spriteSet;
            this.xd = 0.0D;
            this.zd = 0.0D;
            this.vyField = velocityY;
            this.gravity = 0.0F; // no gravity – we'll manually adjust vyField each tick

            this.lifetime = 20; // ~1 second
            this.quadSize = 0.6F;
            this.setSpriteFromAge(spriteSet);
            this.rCol = 1.0F;
            this.gCol = 1.0F;
            this.bCol = 1.0F;
        }

        @Override
        public void tick() {
            if (this.age++ >= this.lifetime) {
                this.remove();
                return;
            }

            // Move strictly upward by vyField, no X/Z displacement
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.y += this.vyField;

            // Slightly damp the upward velocity so particles slow as they rise
            this.vyField *= 0.96D;

            this.setSpriteFromAge(sprites);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_LIT; // use lit sheet so we can force full brightness (glow)
        }

        @Override
        public int getLightColor(float partialTick) {
            return 0xF000F0; // Max brightness so particle appears to glow/emissive
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final SpriteSet spriteSet;

            public Provider(SpriteSet spriteSet) {
                this.spriteSet = spriteSet;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                           double x, double y, double z,
                                           double velocityX, double velocityY, double velocityZ) {
                return new DetectionParticle(world, x, y, z, 0.0D, velocityY, 0.0D, spriteSet);
            }
        }
    }

    // ============================================================
    // Fog Particle (misty fog for onsen atmosphere)
    // ============================================================
    public static class FogParticle extends TextureSheetParticle {
        private final SpriteSet sprites;
        private float targetOpacity;

        protected FogParticle(ClientLevel world, double x, double y, double z,
                              double velocityX, double velocityY, double velocityZ,
                              SpriteSet spriteSet) {
            super(world, x, y, z, 0, 0, 0); // Start with no velocity
            this.sprites = spriteSet;

            // Align to block grid like MistParticle
            this.y = ((int) y) + this.random.nextFloat();

            this.gravity = -0.005F; // Very slight upward drift
            this.lifetime = 100 + this.random.nextInt(60); // Long-lasting (5-8 seconds)
            
            // Reasonable size for good visibility without being overwhelming
            float size = 3.0F + this.random.nextFloat() * 2.0F; // 3-5 blocks size
            this.quadSize = size;
            
            this.targetOpacity = 0.5F + this.random.nextFloat() * 0.2F; // Balanced opacity
            this.alpha = 0; // Start transparent, fade in
            
            this.setSpriteFromAge(spriteSet);

            // Get biome fog color for more natural look
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, y, z);
            java.awt.Color color = new java.awt.Color(world.getBiome(pos).value().getFogColor());
            this.rCol = color.getRed() / 255F;
            this.gCol = color.getGreen() / 255F;
            this.bCol = color.getBlue() / 255F;
            
            // Add random rotation
            this.roll = (float)(Math.PI / 2) * world.random.nextInt(4);
            this.oRoll = this.roll;
            
            // Set initial gentle drift
            this.xd = (this.random.nextFloat() - 0.5F) * 0.005F;
            this.zd = (this.random.nextFloat() - 0.5F) * 0.005F;
            this.yd = 0.002F; // Very slight upward movement
        }

        @Override
        public void tick() {
            super.tick();
            this.setSpriteFromAge(sprites);
            
            // Fade in and out smoothly like MistParticle
            int halfLife = lifetime / 2;
            if (age < halfLife) {
                // Fade in during first half
                this.alpha = ((float) age / halfLife) * targetOpacity;
            } else {
                // Fade out during second half
                this.alpha = ((float) (lifetime - age) / halfLife) * targetOpacity;
            }
            
            // Add subtle random drift changes
            // Keep a slight consistent horizontal drift (no mid-life jitter)
            // xd/zd are set in constructor and left to carry the particle horizontally.
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public static class FogParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public FogParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new FogParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteSet);
        }
    }

    // Mist Weather Fog Particle — same look as FogParticle but no fade-in/out dithering.
    // Holds constant opacity for the bulk of its lifetime so overlapping particles
    // appear as solid mist rather than a flickering alpha pattern.
    // ============================================================
    public static class MistWeatherFogParticle extends TextureSheetParticle {
        private final SpriteSet sprites;
        private float targetOpacity;
        private static final int FADE_TICKS = 30; // smooth in/out

        protected MistWeatherFogParticle(ClientLevel world, double x, double y, double z,
                                         double velocityX, double velocityY, double velocityZ,
                                         SpriteSet spriteSet) {
            super(world, x, y, z, 0, 0, 0);
            this.sprites = spriteSet;

            this.y = ((int) y) + this.random.nextFloat();
            this.gravity = -0.005F;
            this.lifetime = 220 + this.random.nextInt(100);

            // Make mist particles much larger for volumetric feel (8-16 blocks)
            float size = 8.0F + this.random.nextFloat() * 8.0F;
            this.quadSize = size;

            this.targetOpacity = 0.55F + this.random.nextFloat() * 0.15F;
            this.alpha = 0;

            this.setSpriteFromAge(spriteSet);

            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, y, z);
            java.awt.Color color = new java.awt.Color(world.getBiome(pos).value().getFogColor());
            this.rCol = color.getRed() / 255F;
            this.gCol = color.getGreen() / 255F;
            this.bCol = color.getBlue() / 255F;

            this.roll = (float)(Math.PI / 2) * world.random.nextInt(4);
            this.oRoll = this.roll;
            // Slight persistent horizontal drift so each particle subtly moves sideways.
            this.xd = (this.random.nextFloat() - 0.5F) * 0.01F; // ±0.005
            this.zd = (this.random.nextFloat() - 0.5F) * 0.01F; // ±0.005
            this.yd = 0.002F;
        }

        @Override
        public void tick() {
            super.tick();
            // Quick fade-in, constant hold, quick fade-out — no mid-life dithering
            if (age < FADE_TICKS) {
                this.alpha = ((float) age / FADE_TICKS) * targetOpacity;
            } else if (age > lifetime - FADE_TICKS) {
                this.alpha = ((float) (lifetime - age) / FADE_TICKS) * targetOpacity;
            } else {
                this.alpha = targetOpacity;
            }

            // If the player walks into this particle, force a quick fade-out.
            // Use the player's eye position and a slightly larger radius to ensure reliable triggering.
            try {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                net.minecraft.client.player.LocalPlayer pl = mc.player;
                if (pl != null) {
                    double dx = this.x - pl.getX();
                    double dy = this.y - pl.getEyeY();
                    double dz = this.z - pl.getZ();
                    double dist2 = dx * dx + dy * dy + dz * dz;
                    double fadeRadius = 4.0D; // player 'inside' threshold in blocks
                    if (dist2 < fadeRadius * fadeRadius) {
                        if (this.lifetime - this.age > FADE_TICKS) {
                            this.lifetime = this.age + FADE_TICKS; // start quick fade-out
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public static class MistWeatherFogParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public MistWeatherFogParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new MistWeatherFogParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteSet);
        }
    }

    // Flat Ground Fog Particle (horizontal fog layer - faces upward to sky)
    // ============================================================
    public static class FogFlatParticle extends TextureSheetParticle {
        private final SpriteSet sprites;
        private float targetOpacity;
        private int animTicker = 0;
        private static final int ANIM_DELAY = 6; // higher = slower animation

        protected FogFlatParticle(ClientLevel world, double x, double y, double z,
                              SpriteSet spriteSet) {
            super(world, x, y, z, 0, 0, 0);

            this.sprites = spriteSet;
            this.setSpriteFromAge(spriteSet);
            this.quadSize = 4.0F + this.random.nextFloat() * 2.0F; // 4-6 blocks size
            this.lifetime = 300 + this.random.nextInt(200); // 15-25 seconds - much more persistent
            
            this.targetOpacity = 0.4F + this.random.nextFloat() * 0.2F;
            this.alpha = 0.0F; // start transparent, fade in/out

            // Get biome fog color
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, y, z);
            java.awt.Color color = new java.awt.Color(world.getBiome(pos).value().getFogColor());
            this.rCol = color.getRed() / 255F;
            this.gCol = color.getGreen() / 255F;
            this.bCol = color.getBlue() / 255F;

            // Random rotation around center for variety
            this.roll = net.minecraft.util.Mth.HALF_PI * world.random.nextInt(4);
            this.oRoll = this.roll;

            // No movement at all - stays perfectly still
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
            this.gravity = 0.0F;
        }

        @Override
        public void tick() {
            super.tick();
            // Slow sprite animation: only update the sprite every ANIM_DELAY ticks
            this.animTicker++;
            if (this.animTicker % ANIM_DELAY == 0) {
                this.setSpriteFromAge(sprites);
            }

            // Fade in and out smoothly (restored)
            int halfLife = lifetime / 2;
            if (age < halfLife) {
                this.alpha = ((float) age / halfLife) * targetOpacity;
            } else {
                this.alpha = ((float) (lifetime - age) / halfLife) * targetOpacity;
            }

            // Keep particle completely still
            this.xd = 0.0;
            this.yd = 0.0;
            this.zd = 0.0;
        }

        @Override
        public void render(com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, net.minecraft.client.Camera camera, float partialTicks) {
            net.minecraft.world.phys.Vec3 camPos = camera.getPosition();
            float x = (float) (net.minecraft.util.Mth.lerp(partialTicks, this.xo, this.x) - camPos.x());
            float y = (float) (net.minecraft.util.Mth.lerp(partialTicks, this.yo, this.y) - camPos.y());
            float z = (float) (net.minecraft.util.Mth.lerp(partialTicks, this.zo, this.z) - camPos.z());

            // Create quaternion to rotate particle flat (90 degrees around X axis to face upward)
            org.joml.Quaternionf quaternion = new org.joml.Quaternionf(new org.joml.AxisAngle4d(net.minecraft.util.Mth.HALF_PI, -1, 0, 0));
            
            // Apply Z rotation for variety
            quaternion.rotateZ(net.minecraft.util.Mth.lerp(partialTicks, this.oRoll, this.roll));
            
            this.renderRotatedQuad(vertexConsumer, quaternion, x, y, z, partialTicks);
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }
    }

    public static class FogFlatParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public FogFlatParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double velocityX, double velocityY, double velocityZ) {
            return new FogFlatParticle(world, x, y, z, spriteSet);
        }
    }

    // === Yuki Onna Stalking Overlay ===
    private static void yukiOnnaStalkClientTick() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        float target = 0.0F;
        if (!client.isPaused() && player != null && isPlayerBeingStalked(player)) {
            target = 1.0F;
        }
        boolean shouldBeActive = target > 0.0F;
                if (shouldBeActive && !yukiOnnaStalkActive) {
                    com.shioh.sengoku.client.MusicController.setYukiOnnaLocalStalk(true);
                } else if (!shouldBeActive && yukiOnnaStalkActive) {
                    com.shioh.sengoku.client.MusicController.setYukiOnnaLocalStalk(false);
                }
                yukiOnnaStalkActive = shouldBeActive;

        float lerpSpeed = target > yukiOnnaStalkOverlayStrength ? 0.12F : 0.08F;
        yukiOnnaStalkOverlayStrength = Mth.lerp(lerpSpeed, yukiOnnaStalkOverlayStrength, target);
        if (yukiOnnaStalkOverlayStrength < 0.01F) {
            yukiOnnaStalkOverlayStrength = 0.0F;
        }
    }

    private static boolean isPlayerBeingStalked(LocalPlayer player) {
        if (!(player.level() instanceof ClientLevel clientLevel)) {
            return false;
        }
        java.util.UUID playerId = player.getUUID();
        double searchRadius = 96.0D;
        for (YukiOnnaEntity entity : clientLevel.getEntitiesOfClass(YukiOnnaEntity.class, player.getBoundingBox().inflate(searchRadius))) {
            if (!entity.isRemoved() && entity.isCreepy() && entity.getStalkingTargetUuid().filter(playerId::equals).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static void renderYukiOnnaStalkOverlay(GuiGraphics guiGraphics) {
        if (yukiOnnaStalkOverlayStrength <= 0.001F) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || client.options.hideGui) {
            return;
        }

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
    float alpha = yukiOnnaStalkOverlayStrength * 0.35F;

        renderFullScreenOverlay(guiGraphics, STEALTH_VIGNETTE, 0.92F, 0.92F, 0.92F, alpha);
    }

    // === Concealment vignette (when player is hidden in grass/flowers) ===
    private static void concealClientTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.isPaused()) return;
        LocalPlayer player = client.player;
        if (player == null) return;
        // Respect config toggle: if disabled, ensure vignette is off
        com.shioh.sengoku.config.SengokuConfig cfg = com.shioh.sengoku.config.SengokuConfig.getInstance();
        if (!cfg.concealmentVignetteEnabled) {
            concealVignetteStrength = 0.0F;
            concealVignetteActive = false;
            return;
        }

        float target = 0.0F;
        if (!player.isSpectator()) {
            Level level = player.level();
            BlockPos foot = player.blockPosition();
            BlockPos head = foot.above();

            // Strong concealment: tall plants and tall grass variants
            boolean strong = false;
            try {
                strong = level.getBlockState(foot).is(Blocks.LILAC)
                    || level.getBlockState(head).is(Blocks.LILAC)
                    || level.getBlockState(foot).is(Blocks.PEONY)
                    || level.getBlockState(head).is(Blocks.PEONY)
                    || level.getBlockState(foot).is(Blocks.ROSE_BUSH)
                    || level.getBlockState(head).is(Blocks.ROSE_BUSH)
                    || level.getBlockState(foot).is(Blocks.SUNFLOWER)
                    || level.getBlockState(head).is(Blocks.SUNFLOWER)
                    || level.getBlockState(foot).is(Blocks.TALL_GRASS)
                    || level.getBlockState(head).is(Blocks.TALL_GRASS)
                    || level.getBlockState(foot).is(Blocks.LARGE_FERN)
                    || level.getBlockState(head).is(Blocks.LARGE_FERN);
            } catch (Throwable ignored) {
                strong = false;
            }

            if (strong) {
                target = 1.0F;
            } else {
                // Medium concealment: small flowers that give partial conceal
                boolean medium = false;
                try {
                    medium = level.getBlockState(foot).is(Blocks.LILY_OF_THE_VALLEY)
                        || level.getBlockState(head).is(Blocks.LILY_OF_THE_VALLEY)
                        || level.getBlockState(foot).is(Blocks.BLUE_ORCHID)
                        || level.getBlockState(head).is(Blocks.BLUE_ORCHID);
                } catch (Throwable ignored) {
                    medium = false;
                }
                if (medium) target = 0.9F;
            }
        }

        // Also show vignette when player is invisible (potion or effect)
        try {
            if (player.isInvisible()) {
                // Treat invisibility as full concealment for the vignette
                target = Math.max(target, 1.0F);
            }
        } catch (Throwable ignored) {}

        boolean shouldBeActive = target > 0.0F;
        concealVignetteActive = shouldBeActive;

        float lerpSpeed = target > concealVignetteStrength ? 0.12F : 0.08F;
        concealVignetteStrength = Mth.lerp(lerpSpeed, concealVignetteStrength, target);
        if (concealVignetteStrength < 0.01F) concealVignetteStrength = 0.0F;
    }

    private static void renderConcealOverlay(GuiGraphics guiGraphics) {
        // Respect config toggle
        if (!com.shioh.sengoku.config.SengokuConfig.getInstance().concealmentVignetteEnabled) return;
        if (concealVignetteStrength <= 0.001F) return;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || client.options.hideGui) return;

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        // Draw only the vignette texture tinted black at low alpha so center remains unchanged.
        float maskAlpha = concealVignetteStrength * 0.18F; // much more subtle

        renderFullScreenOverlay(guiGraphics, STEALTH_VIGNETTE, 1.0F, 1.0F, 1.0F, maskAlpha);
    }

    private static void renderPoiseMeter(GuiGraphics guiGraphics) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || client.options.hideGui) return;

        float damageRatio = Mth.clamp(playerPoiseCurrent / Math.max(playerPoiseMax, 0.001F), 0.0F, 1.0F);
        float ratio = damageRatio;
        if (SHOW_POISE_WHEN_DAMAGED_ONLY && damageRatio <= 0.001F) return;

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        int centerX = width / 2;
        int barY = height - 54;
        int barHeight = 4;
        int halfBarWidth = 44;
        int minHalfFill = 2;
        int filledHalfWidth = minHalfFill + Math.round((halfBarWidth - minHalfFill) * ratio);

        int bgColor = 0x90000000;
        int fillColor = 0xFFE9F1F5;
        int centerColor = 0xFFF6FBFF;

        guiGraphics.fill(centerX - halfBarWidth, barY, centerX + halfBarWidth + 1, barY + barHeight, bgColor);
        if (filledHalfWidth > 0) {
            guiGraphics.fill(centerX - filledHalfWidth, barY, centerX + filledHalfWidth + 1, barY + barHeight, fillColor);
        }
        guiGraphics.fill(centerX, barY - 1, centerX + 1, barY + barHeight + 1, centerColor);
    }

    // (stealth cooldown UI removed — using vanilla item cooldown visuals)

    // === Warm Water Client System ===
    private static final int MAGMA_DETECTION_RADIUS = 3;
    private static boolean isPlayerInWarmWater = false;
    private static int warmWaterCheckCooldown = 0;

    // Cache of warm-water block positions (packed longs) rebuilt every WARM_WATER_CACHE_REFRESH_TICKS
    // ticks or when the player moves too far. volatile reference swap is safe for render-thread reads.
    private static volatile java.util.Set<Long> warmWaterSurfaceCache = java.util.Collections.emptySet();
    private static BlockPos warmWaterCacheCenter = null;
    private static int warmWaterCacheCountdown = 0;
    private static final int WARM_WATER_CACHE_REFRESH_TICKS = 40; // 2 seconds
    private static final int WARM_WATER_CACHE_INVALIDATE_SQ  = 16 * 16; // trigger rebuild if moved >16 blocks

    /** Called when the player changes the particle radius slider so the cache rebuilds immediately. */
    public static void invalidateWarmWaterCache() {
        warmWaterCacheCenter = null;
        warmWaterSurfaceCache = java.util.Collections.emptySet();
    }
    
    /**
     * Client tick handler for warm water detection
     */
    private static void warmWaterClientTick() {
        Minecraft client = Minecraft.getInstance();
        if (!com.shioh.sengoku.config.SengokuConfig.getInstance().warmWaterEnabled) return;
        if (client.isPaused()) return;

        LocalPlayer player = client.player;
        if (player == null) return;

        if (--warmWaterCheckCooldown > 0) return;
        warmWaterCheckCooldown = 10;

        ClientLevel level = (ClientLevel) player.level();
        if (level.dimension() == Level.NETHER) {
            isPlayerInWarmWater = false;
            invalidateWarmWaterCache();
            return;
        }

        BlockPos playerPos = player.blockPosition();

        // Rebuild the surface cache when it expires or the player has moved significantly
        boolean cacheStale = warmWaterCacheCenter == null
            || warmWaterCacheCenter.distSqr(playerPos) > WARM_WATER_CACHE_INVALIDATE_SQ
            || --warmWaterCacheCountdown <= 0;
        if (cacheStale) {
            warmWaterCacheCountdown = WARM_WATER_CACHE_REFRESH_TICKS;
            refreshWarmWaterCache(level, playerPos);
        }

        // Local scan for player-in-water check (small radius, already throttled to 10 ticks)
        isPlayerInWarmWater = player.isInWater() && isNearMagmaBlock(level, playerPos);

        // Spawn ambient particles from the cached positions — no per-tick block scan needed
        spawnAmbientWarmWaterParticles(level, player);
    }

    /**
     * Rebuilds the warm-water block position cache around the player.
     * Two-pass: find magma blocks first, then collect all water blocks near each one.
     * This replaces the 320-radius column scan with a targeted magma-first approach.
     */
    private static void refreshWarmWaterCache(ClientLevel level, BlockPos center) {
        if (level.dimension() == Level.NETHER) {
            warmWaterSurfaceCache = java.util.Collections.emptySet();
            warmWaterCacheCenter = center.immutable();
            return;
        }

        java.util.Set<Long> newCache = new java.util.HashSet<>();
        // Use config radius (clamped to a safe min) so the slider drives the actual scan area
        final int scanRadius = Math.max(16, com.shioh.sengoku.config.SengokuConfig.getInstance().warmWaterParticleRadius);
        final int minY = Math.max(center.getY() - 30, level.getMinBuildHeight());
        final int maxY = Math.min(center.getY() + 8,  level.getMaxBuildHeight() - 1);

        // Pass 1: locate magma blocks in the area (stride-2 keeps cost manageable)
        java.util.List<BlockPos> magmaList = new java.util.ArrayList<>();
        for (int dx = -scanRadius; dx <= scanRadius; dx += 2) {
            for (int dz = -scanRadius; dz <= scanRadius; dz += 2) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (level.isLoaded(p) && level.getBlockState(p).is(Blocks.MAGMA_BLOCK)) {
                        magmaList.add(p.immutable());
                    }
                }
            }
        }

        // Pass 2: collect only SURFACE water blocks within detection reach of each magma block.
        // Surface = water block where the block directly above is NOT water.
        // Storing only surface blocks ensures particles always spawn at the visible water top
        // and budget is never wasted on invisible sub-surface positions.
        final int det = MAGMA_DETECTION_RADIUS + 1;
        for (BlockPos magma : magmaList) {
            for (int dx = -det; dx <= det; dx++) {
                for (int dz = -det; dz <= det; dz++) {
                    for (int dy = -2; dy <= det + 4; dy++) {
                        BlockPos check = magma.offset(dx, dy, dz);
                        if (level.isLoaded(check)
                                && level.getFluidState(check).getType() == Fluids.WATER
                                && level.getFluidState(check.above()).getType() != Fluids.WATER) {
                            newCache.add(check.asLong());
                        }
                    }
                }
            }
        }

        // Atomic reference swap — the render thread always sees a complete, consistent set
        warmWaterSurfaceCache = java.util.Collections.unmodifiableSet(newCache);
        warmWaterCacheCenter = center.immutable();
    }
    
    /**
     * Check if there are magma blocks within the detection radius
     */
    private static boolean isNearMagmaBlock(Level level, BlockPos centerPos) {
        if (level.dimension() == Level.NETHER) {
            return false;
        }

        for (int x = -MAGMA_DETECTION_RADIUS; x <= MAGMA_DETECTION_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -MAGMA_DETECTION_RADIUS; z <= MAGMA_DETECTION_RADIUS; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(Blocks.MAGMA_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if a specific water block should be rendered as warm water
     */
    public static boolean isWarmWaterAt(Level level, BlockPos pos) {
        if (level.dimension() == Level.NETHER) return false;
        // O(1) cache lookup — safe to call from the fluid renderer mixin on the render thread.
        // warmWaterSurfaceCache is a volatile reference to an unmodifiable set; no locking needed.
        if (!isPlayerInWarmWater) return false;
        return warmWaterSurfaceCache.contains(pos.asLong());
    }
    
    /**
     * Get the warm water color multiplier (orange/red tint)
     * Returns RGB values that should be multiplied with the water color
     */
    public static int getWarmWaterColor() {
        // Milky turquoise-white for hot spring look (more opaque appearance)
        return 0xE8F4F8; // Very light blue-white, like mineral-rich hot spring water
    }
    
    /**
     * Get the opacity multiplier for warm water
     * Returns a value between 0.0 and 1.0
     */
    public static float getWarmWaterOpacity() {
        return 0.85f; // More opaque look when applied via fog/overlays
    }
    
    /**
     * Check if player is currently in warm water (for external use)
     */
    public static boolean isPlayerInWarmWater() {
        return isPlayerInWarmWater;
    }

    // ==============================================
    // Client-only ambient particle spawning utilities
    // ==============================================
    private static void spawnAmbientWarmWaterParticles(ClientLevel level, LocalPlayer player) {
        java.util.Set<Long> cache = warmWaterSurfaceCache;
        if (cache.isEmpty()) return;

        double px = player.getX();
        double pz = player.getZ();

        // Sort positions closest-first so the per-tick caps create a smooth distance
        // falloff instead of random holes across the spring area.
        long[] sorted = new long[cache.size()];
        int idx = 0;
        for (long packed : cache) sorted[idx++] = packed;
        // Insertion sort is fine here; spring pools are small (typically <500 blocks).
        for (int i = 1; i < idx; i++) {
            long key = sorted[i];
            BlockPos kp = BlockPos.of(key);
            double kd = (kp.getX() + 0.5 - px) * (kp.getX() + 0.5 - px)
                      + (kp.getZ() + 0.5 - pz) * (kp.getZ() + 0.5 - pz);
            int j = i - 1;
            while (j >= 0) {
                BlockPos jp = BlockPos.of(sorted[j]);
                double jd = (jp.getX() + 0.5 - px) * (jp.getX() + 0.5 - px)
                          + (jp.getZ() + 0.5 - pz) * (jp.getZ() + 0.5 - pz);
                if (jd <= kd) break;
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = key;
        }

        int smokeSpawned = 0, fogSpawned = 0, fogFlatSpawned = 0;
        final int maxSmokePerTick   = 8;
        final int maxFogPerTick     = 20;
        final int maxFogFlatPerTick = 12;

        for (int i = 0; i < idx; i++) {
            if (smokeSpawned >= maxSmokePerTick
                    && fogSpawned >= maxFogPerTick
                    && fogFlatSpawned >= maxFogFlatPerTick) break;

            BlockPos waterPos = BlockPos.of(sorted[i]);
            int hx = waterPos.getX();
            int hz = waterPos.getZ();
            double dxw = hx + 0.5 - px;
            double dzw = hz + 0.5 - pz;
            double dist2 = dxw * dxw + dzw * dzw;

            // Smoke: 1-in-6 columns, only occasionally
            if (smokeSpawned < maxSmokePerTick) {
                int selector = Math.floorMod((hx * 734287) ^ (hz * 91273), 6);
                if (selector == 0 && level.random.nextInt(4) == 0) {
                    createCampfireSmokeAtSurfaceClient(level, waterPos);
                    smokeSpawned++;
                }
            }

            // Fog: 1-in-3 surface blocks per tick
            if (fogSpawned < maxFogPerTick) {
                int selector = Math.floorMod((hx * 193939) ^ (hz * 75353), 3);
                if (selector == 0) {
                    createMistyFogAtSurfaceClient(level, waterPos);
                    fogSpawned++;
                }
            }

            // Flat fog: 1-in-4 surface blocks per tick
            if (fogFlatSpawned < maxFogFlatPerTick) {
                int selector = Math.floorMod((hx * 31337) ^ (hz * 17239), 4);
                if (selector == 0) {
                    createFlatFogAtSurfaceClient(level, waterPos);
                    fogFlatSpawned++;
                }
            }
        }
    }


    private static void createCampfireSmokeAtSurfaceClient(ClientLevel level, BlockPos waterPos) {
        // Find the first air block above water (the surface)
        BlockPos surfacePos = waterPos;
        for (int y = 0; y < 64; y++) {
            BlockPos checkPos = waterPos.offset(0, y, 0);
            if (level.getFluidState(checkPos).getType() != Fluids.WATER) {
                surfacePos = checkPos.below();
                break;
            }
        }

        double sx = surfacePos.getX() + 0.5;
        double sy = surfacePos.getY() + 3.0; // three blocks above water for better visibility
        double sz = surfacePos.getZ() + 0.5;

        // Taller plume: spawn several segments with upward velocity
        int segments = 3; // connected but lighter on performance
        for (int s = 0; s < segments; s++) {
            double py = sy + s * 0.9; // connected look
            double jx = sx + (level.random.nextDouble() - 0.5) * 0.2;
            double jz = sz + (level.random.nextDouble() - 0.5) * 0.2;
            double vx = (level.random.nextDouble() - 0.5) * 0.005;
            double vz = (level.random.nextDouble() - 0.5) * 0.005;
            double vy = 0.05 + level.random.nextDouble() * 0.05; // steady upward push
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, jx, py, jz, vx, vy, vz);
        }

        // Occasionally add cosy smoke at the base for connection without constant overhead
        if (level.random.nextInt(3) == 0) {
            double baseVy = 0.015 + level.random.nextDouble() * 0.02;
            double baseJx = sx + (level.random.nextDouble() - 0.5) * 0.2;
            double baseJz = sz + (level.random.nextDouble() - 0.5) * 0.2;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, baseJx, sy, baseJz, 0.0, baseVy, 0.0);
        }
    }
    
    /**
     * Spawn custom fog particles at water surface (client-side)
     */
    private static void createMistyFogAtSurfaceClient(ClientLevel level, BlockPos waterPos) {
        // Respect client config: allow users to disable weather fog particles
        if (!com.shioh.sengoku.config.SengokuConfig.getInstance().fogParticlesEnabled) return;
        // Spawn fog particle hovering just above the water surface
        double x = waterPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
        double y = waterPos.getY() + 0.9; // hover just above surface
        double z = waterPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
        
        // Avoid spawning directly on leaves or immediately under/above leaf layers
        BlockPos center = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
        try {
            boolean foundLeaves = false;
            for (int dy = -1; dy <= 3; dy++) {
                net.minecraft.world.level.block.state.BlockState bs = level.getBlockState(center.offset(0, dy, 0));
                if (bs.is(BlockTags.LEAVES) || bs.getBlock() instanceof LeavesBlock) { foundLeaves = true; break; }
            }
            if (foundLeaves) return;
        } catch (Throwable ignored) {}

        // Use overrideDistance=true (force parameter) to render at extreme distances
        level.addParticle(ParticleRegistry.FOG, true, x, y, z, 0.0, 0.0, 0.0);
    }
    
    /**
     * Spawn flat fog particles at water surface (client-side) - horizontal layer facing sky
     */
    private static void createFlatFogAtSurfaceClient(ClientLevel level, BlockPos waterPos) {
        // Respect client config: allow users to disable weather fog particles
        if (!com.shioh.sengoku.config.SengokuConfig.getInstance().fogParticlesEnabled) return;
        // Get precise surface height using heightmap (ignore leaves) and add random offset to prevent z-fighting
        int surfaceHeight = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, waterPos.getX(), waterPos.getZ());
        double x = waterPos.getX() + level.random.nextFloat();
        double y = surfaceHeight + level.random.nextFloat(); // random height offset prevents z-fighting
        double z = waterPos.getZ() + level.random.nextFloat();
        
        // Avoid spawning directly on leaves
        BlockPos checkPos = new BlockPos((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
        try {
            net.minecraft.world.level.block.state.BlockState bs = level.getBlockState(checkPos);
            if (bs.is(BlockTags.LEAVES) || bs.getBlock() instanceof LeavesBlock) return;
        } catch (Throwable ignored) {}

        // Use overrideDistance=true (force parameter) to render at extreme distances
        level.addParticle(ParticleRegistry.FOG_FLAT, true, x, y, z, 0.0, 0.0, 0.0);
    }
    
    // === Mist Weather Client System ===
    /**
        * Client tick handler for mist weather.
        * Keeps the existing flat layer and adds a distant volumetric layer.
     */
    private static void mistWeatherClientTick() {
        Minecraft client = Minecraft.getInstance();
        // Allow users to toggle off mist/fog particle rendering in visuals config
        if (!com.shioh.sengoku.config.SengokuConfig.getInstance().fogParticlesEnabled) return;
        
        // Don't spawn particles when game is paused or player is alt-tabbed
        if (client.isPaused()) return;
        
        LocalPlayer player = client.player;
        if (player == null) return;

        double playerX = player.getX();
        double playerZ = player.getZ();

        double horizontalSpeed = 0.0D;
        if (!Double.isNaN(mistLastPlayerX) && !Double.isNaN(mistLastPlayerZ)) {
            double mdx = playerX - mistLastPlayerX;
            double mdz = playerZ - mistLastPlayerZ;
            horizontalSpeed = Math.sqrt(mdx * mdx + mdz * mdz);
        }
        mistLastPlayerX = playerX;
        mistLastPlayerZ = playerZ;

        if (player.level().dimension() != Level.OVERWORLD) {
            return;
        }
        
        // Check if mist weather is active
        if (!com.shioh.sengoku.system.MistWeatherSystem.isMisty(player.level())) {
            return;
        }
        
        ClientLevel level = (ClientLevel) player.level();
        BlockPos playerPos = player.blockPosition();
        
        // Existing flat ground-fog layer (preserved as requested)
        final int radius = 48; // Reduced from 64 to 48 blocks
        final int stride = 8; // Increased from 4 to 8 (check every 8 blocks)
        final int maxParticlesPerTick = 20; // Reduced from 50 to 20
        
        int particlesSpawned = 0;
        
        for (int dx = -radius; dx <= radius; dx += stride) {
            for (int dz = -radius; dz <= radius; dz += stride) {
                if (particlesSpawned >= maxParticlesPerTick) break;
                
                // Random chance to spawn (reduced from 20% to 10%)
                if (level.random.nextFloat() > 0.1f) continue;
                
                int x = playerPos.getX() + dx;
                int z = playerPos.getZ() + dz;
                
                // Get surface height
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
                
                // Only spawn on solid ground (not in water or air)
                BlockPos surfacePos = new BlockPos(x, surfaceY - 1, z);
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(surfacePos);
                if (state.isAir() || !state.getFluidState().isEmpty()) {
                    continue;
                }
                
                // Spawn flat fog particle at surface (skip if leaves present)
                double px = x + level.random.nextFloat();
                double py = surfaceY + level.random.nextFloat();
                double pz = z + level.random.nextFloat();
                BlockPos centerChk = new BlockPos((int)Math.floor(px), (int)Math.floor(py), (int)Math.floor(pz));
                try {
                    boolean foundLeaves = false;
                    for (int dy = -1; dy <= 3; dy++) {
                        net.minecraft.world.level.block.state.BlockState bs = level.getBlockState(centerChk.offset(0, dy, 0));
                        if (bs.is(BlockTags.LEAVES) || bs.getBlock() instanceof LeavesBlock) { foundLeaves = true; break; }
                    }
                    if (foundLeaves) continue;
                } catch (Throwable ignored) {}

                level.addParticle(ParticleRegistry.FOG_FLAT, true, px, py, pz, 0.0, 0.0, 0.0);
                particlesSpawned++;
            }
            if (particlesSpawned >= maxParticlesPerTick) break;
        }

        // Additional mist volume layer using non-flat fog particles.
        com.shioh.sengoku.config.SengokuConfig cfg = com.shioh.sengoku.config.SengokuConfig.getInstance();
        if (!cfg.mistFogParticlesEnabled) {
            return;
        }

        final int minRadius = 24;
        // Config controls the max radius for FOG_MIST only; clamp to stay sensible.
        int configuredMax = Math.max(8, Math.min(128, cfg.mistFogMaxRange));
        final int maxRadius = Math.max(minRadius + 2, configuredMax);
        int fogPerTick = 5;

        try {
            net.minecraft.client.ParticleStatus p = client.options.particles().get();
            if (p == net.minecraft.client.ParticleStatus.DECREASED) {
                fogPerTick = 4;
            } else if (p == net.minecraft.client.ParticleStatus.MINIMAL) {
                fogPerTick = 3;
            }
        } catch (Throwable ignored) {}

        // Reduce volumetric (non-flat) fog spawn rate when many entities are being rendered.
        // Flat ground fog remains unchanged.
        int renderEntityCount = getApproxRenderableEntityCount(client, level, player);
        float entityScale = 1.0f;
        if (renderEntityCount >= 220) {
            entityScale = 0.25f;
        } else if (renderEntityCount >= 160) {
            entityScale = 0.40f;
        } else if (renderEntityCount >= 100) {
            entityScale = 0.60f;
        } else if (renderEntityCount >= 60) {
            entityScale = 0.80f;
        }
        fogPerTick = Math.max(1, Math.round(fogPerTick * entityScale));

        // Give mist a small, capped burst while moving so it keeps pace with sprinting.
        // Keep the cap tight to avoid turning movement into a particle spike.
        int movementBoost = 0;
        if (horizontalSpeed > 0.02D) {
            movementBoost = Math.min(5, (int) Math.floor((horizontalSpeed - 0.02D) * 24.0D));
        }
        fogPerTick = Math.min(10, fogPerTick + movementBoost);

        // Keep a minimum distance between mist spawns in this tick so particles don't stack.
        final double minSeparation = 4.0D;
        final double minSeparationSq = minSeparation * minSeparation;
        java.util.List<double[]> chosenSpawns = new java.util.ArrayList<>();

        int fogSpawned = 0;
        int attempts = 0;
        int maxAttempts = fogPerTick * 6;

        // Bias the cloud slightly toward movement direction so forward motion stays encapsulated.
        net.minecraft.world.phys.Vec3 velocity = player.getDeltaMovement();
        double velLen = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double leadScale = Math.min(10.0D, Math.max(0.0D, velLen * 18.0D));
        double leadX = velLen > 1.0E-4D ? (velocity.x / velLen) * leadScale : 0.0D;
        double leadZ = velLen > 1.0E-4D ? (velocity.z / velLen) * leadScale : 0.0D;
        double centerX = playerX + leadX;
        double centerZ = playerZ + leadZ;

        // Most spawns are near the player for immediate coverage; some remain distant for atmosphere.
final double nearMin = 20.0D;
final double nearMax = Math.max(nearMin + 4.0D, Math.min(maxRadius * 0.6D, 48.0D));
final double nearChance = 0.65D;

        while (fogSpawned < fogPerTick && attempts < maxAttempts) {
            attempts++;

            // Uniform ring distribution: sqrt for area-correct radial sampling.
            boolean nearSample = level.random.nextDouble() < nearChance;
            double ringMin = nearSample ? nearMin : minRadius;
            double ringMax = nearSample ? nearMax : maxRadius;
            double r = ringMin + (ringMax - ringMin) * Math.sqrt(level.random.nextDouble());
            double a = level.random.nextDouble() * (Math.PI * 2.0);
            double px = centerX + Math.cos(a) * r;
            double pz = centerZ + Math.sin(a) * r;

            // Use same heightmap logic as flat fog: only spawn where there's solid ground
            int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                (int)Math.floor(px), (int)Math.floor(pz));
            BlockPos groundCheck = new BlockPos((int)Math.floor(px), surfaceY - 1, (int)Math.floor(pz));
            net.minecraft.world.level.block.state.BlockState groundState = level.getBlockState(groundCheck);
            if (groundState.isAir() || !groundState.getFluidState().isEmpty()) {
                continue; // Skip if no solid ground
            }

            // Spawn mist at varied heights above the solid surface
            double py = surfaceY + level.random.nextDouble() * 4.0; // 0-4 blocks above ground

            boolean tooClose = false;
            for (double[] other : chosenSpawns) {
                double dx = px - other[0];
                double dy = py - other[1];
                double dz = pz - other[2];
                if (dx * dx + dy * dy + dz * dz < minSeparationSq) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            chosenSpawns.add(new double[]{px, py, pz});
            level.addParticle(ParticleRegistry.FOG_MIST, true, px, py, pz, 0.0, 0.0, 0.0);
            fogSpawned++;
        }
    }

    private static int getApproxRenderableEntityCount(Minecraft client, ClientLevel level, LocalPlayer player) {
        int tickNow = player.tickCount;
        if (tickNow - cachedMistRenderableEntitySampleTick < 20) {
            return cachedMistRenderableEntityCount;
        }

        cachedMistRenderableEntitySampleTick = tickNow;
        int count = 0;

        double renderRangeBlocks = Math.max(8, client.options.getEffectiveRenderDistance()) * 16.0;
        double renderRangeSq = renderRangeBlocks * renderRangeBlocks;

        try {
            for (net.minecraft.world.entity.Entity entity : level.entitiesForRendering()) {
                if (entity == null || entity.isRemoved() || entity == player) continue;
                if (player.distanceToSqr(entity) > renderRangeSq) continue;
                count++;
                if (count >= 400) break; // cap counting cost in extreme scenarios
            }
        } catch (Throwable ignored) {
        }

        cachedMistRenderableEntityCount = count;
        return count;
    }
}




