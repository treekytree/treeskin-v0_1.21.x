package net.treekytree.treeskin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TreeskinConfig {
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_PATH = MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config")
            .resolve("treeskin.json");

    private static Map<String, String> skinUrls = new HashMap<>();

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    Type type = new TypeToken<Map<String, String>>() {}.getType();
                    skinUrls = GSON.fromJson(reader, type);
                    if (skinUrls == null) skinUrls = new HashMap<>();
                }
            } else {
                save(); // create default file
            }
        } catch (IOException e) {
            System.err.println("[TreeSkin] Failed to load config: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(skinUrls, writer);
            }
        } catch (IOException e) {
            System.err.println("[TreeSkin] Failed to save config: " + e);
        }
    }

    public static void setSkinUrl(String state, String url) {
        skinUrls.put(state.toLowerCase(), url);
        save();
    }

    public static String getSkinUrl(String state) {
        return skinUrls.get(state.toLowerCase());
    }
}
