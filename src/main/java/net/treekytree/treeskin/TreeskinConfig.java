package net.treekytree.treeskin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TreeskinConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static Map<String, String> skinUrls = new HashMap<>();
    private static String skinType = "classic"; // default

    public static void load() {
        try {
            Path configDir = MinecraftClient.getInstance().runDirectory.toPath()
                    .resolve("config")
                    .resolve("treeskin");
            Files.createDirectories(configDir);

            configPath = configDir.resolve("config.json");
            if (!Files.exists(configPath)) {
                save();
                return;
            }

            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = GSON.fromJson(new FileReader(configPath.toFile()), type);

            if (data.containsKey("skinUrls")) {
                Map<String, String> loadedUrls = (Map<String, String>) data.get("skinUrls");
                skinUrls.putAll(loadedUrls);
            }

            if (data.containsKey("skinType")) {
                skinType = data.get("skinType").toString();
            }
        } catch (Exception e) {
            System.err.println("[TreeSkin] Failed to load config: " + e);
        }
    }

    public static void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("skinUrls", skinUrls);
            data.put("skinType", skinType);
            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[TreeSkin] Failed to save config: " + e);
        }
    }

    public static void setSkinUrl(String state, String url) {
        skinUrls.put(state, url);
        save();
    }

    public static String getSkinUrl(String state) {
        return skinUrls.get(state);
    }

    public static String getSkinType() {
        return skinType;
    }

    public static void setSkinType(String type) {
        skinType = type.equalsIgnoreCase("slim") ? "slim" : "classic";
        save();
    }
}
