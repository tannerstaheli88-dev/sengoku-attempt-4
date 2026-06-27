package com.shioh.sengoku;

import net.minecraft.resources.ResourceLocation;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Constants {

	public static final String MOD_ID = "sengoku";
	public static final String MOD_NAME = "Sengoku Jidai";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static ResourceLocation ID(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static final String PLAYER_ENTITY_INTERACTION_RANGE_MODIFIER_ID = "74a196e4-cd9e-4c93-8606-8e7f0afdc959";

	// Centralized toggle for Door AI debug logs
	public static boolean isDoorAiDebugEnabled() {
		try {
			String v = System.getProperty("sengoku.debug.doorai", "");
			if (!v.isEmpty()) return v.equalsIgnoreCase("true") || v.equals("1");
		} catch (Throwable ignored) {}
		try {
			return FabricLoader.getInstance().isDevelopmentEnvironment();
		} catch (Throwable ignored) {}
		return false;
	}
}