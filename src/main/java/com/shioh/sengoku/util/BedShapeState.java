package com.shioh.sengoku.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BedShapeState {
    public static boolean needsToBeChecked = false;
    public static boolean isPillowedPackActive = false;
    public static boolean isPillowedConnectedPackActive = false;
    public static boolean silent = false;

    //some bs i stole from morebedvariants to make the grass bed

    @Environment(EnvType.CLIENT)
    public static void checkClientResourcepacks() {
        String clientutilClassName = "com.shioh.sengoku.util.client.Clientutil";
        String packCheckerMethodName = "checkActiveResourcePacks";
        try {
            Class<?> clientutilClass = Class.forName(clientutilClassName);
            clientutilClass.getMethod(packCheckerMethodName).invoke(null);
            if (!silent) {
                sengokuFabric.LOGGER.info("Active resource packs checked successfully.");
                silent = true;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            try {
                Class<?> clientutilClass = Class.forName(clientutilClassName);
                clientutilClass.getMethod(packCheckerMethodName).invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e1) {
                sengokuFabric.LOGGER.warn("Failed to check active resource packs: " + e.getMessage());
            }
            BedShapeState.needsToBeChecked = false;
        }
    }

    @Environment(EnvType.SERVER)
    public static class ServerConfig {
        private static final File folder = new File("config");
        private static File mBedVConfig;
        public static Gson configGson = new GsonBuilder().setPrettyPrinting().create();
        private static final String pillowedName = "pillowedUnconnectedShape";
        private static final String pillowedConnectedName = "pillowedConnectedShape";

        public static void checkServerConfig() {
            createServerConfig();
            readServerConfig();
        }

        private static void createServerConfig() {
            if (!folder.exists()) {
                folder.mkdir();
            }
            if (folder.isDirectory()) {
                mBedVConfig = new File(folder,"morebedvariants.json");
                boolean seemsValid;
                if (mBedVConfig.exists()) {
                    try {
                        String mBedVConfigJson = Files.readString(Path.of(mBedVConfig.getPath()));
                        seemsValid = mBedVConfigJson.trim().startsWith("{");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else { seemsValid = true; }
                if (!mBedVConfig.exists() || !seemsValid) {
                    if (!seemsValid) {sengokuFabric.LOGGER.info("Found invalid config file, creating new config file at './config/morebedvariants.json'.");}
                    try {
                        mBedVConfig.createNewFile();
                        JsonObject defaultConfig = new JsonObject();
                        defaultConfig.addProperty(pillowedName, false);
                        defaultConfig.addProperty(pillowedConnectedName, false);
                        try (FileWriter writer = new FileWriter(mBedVConfig)) {
                            configGson.toJson(defaultConfig, writer);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        private static void readServerConfig() {
            try {
                JsonReader mBedVConfigJsonReader = configGson.newJsonReader(new FileReader(mBedVConfig));
                JsonObject config = JsonParser.parseReader(mBedVConfigJsonReader).getAsJsonObject();
                isPillowedPackActive = config.get(pillowedName).getAsBoolean();
                isPillowedConnectedPackActive = config.get(pillowedConnectedName).getAsBoolean();
                needsToBeChecked = false;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public static void writeServerConfig() {
            boolean isPillowedConnectedShape = isPillowedConnectedPackActive;
            boolean isPillowedShape = !isPillowedConnectedShape && isPillowedPackActive;
            mBedVConfig = new File(folder,"morebedvariants.json");
            try {
                JsonObject config = new JsonObject();
                config.addProperty(pillowedName, isPillowedShape);
                config.addProperty(pillowedConnectedName, isPillowedConnectedShape);
                try (FileWriter writer = new FileWriter(mBedVConfig)) {
                    configGson.toJson(config, writer);
                }

            } catch (IOException e) {
                sengokuFabric.LOGGER.warn("Failed to write bed shape settings to config file at './config/morebedvariants.json': {}", e.getMessage());
            }
            isPillowedPackActive = isPillowedShape;
            isPillowedConnectedPackActive = isPillowedConnectedShape;
        }
    }
}