package net.treekytree.treeskin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class TreeskinConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/treeskin.json");

    private static SkinType skinType = SkinType.CLASSIC;
    private static TriggerMode triggerMode = TriggerMode.ENTER;
    private static boolean showNotifications = true;
    private static long stillThresholdMs = 5000;

    private static final HashMap<String, String> skinUrls = new HashMap<>();

    public static void load() {
        try {
            if (!FILE.exists()) {
                save();
                return;
            }

            FileReader reader = new FileReader(FILE);
            SavedConfig cfg = GSON.fromJson(reader, SavedConfig.class);
            reader.close();

            if (cfg == null) return;

            skinType = parseEnum(cfg.skinType, SkinType.CLASSIC);
            triggerMode = parseEnum(cfg.triggerMode, TriggerMode.ENTER);
            showNotifications = cfg.showNotifications;
            stillThresholdMs = cfg.stillThresholdMs;

            skinUrls.clear();
            if (cfg.skinUrls != null) skinUrls.putAll(cfg.skinUrls);

        } catch (Exception e) {
            System.err.println("[TreeSkin] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            SavedConfig cfg = new SavedConfig();
            cfg.skinType = skinType.name();
            cfg.triggerMode = triggerMode.name();
            cfg.showNotifications = showNotifications;
            cfg.stillThresholdMs = stillThresholdMs;
            cfg.skinUrls = skinUrls;

            File parent = FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                boolean created = parent.mkdirs();
                if (!created) {
                    System.err.println("[TreeSkin] Failed to create config directory.");
                }
            }

            FileWriter writer = new FileWriter(FILE);
            GSON.toJson(cfg, writer);
            writer.close();

        } catch (Exception e) {
            System.err.println("[TreeSkin] Failed to save config: " + e.getMessage());
        }
    }

    private static <T extends Enum<T>> T parseEnum(String value, T fallback) {
        try {
            return Enum.valueOf(fallback.getDeclaringClass(), value.toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    // Getters / setters

    public static SkinType getSkinType() {
        return skinType;
    }

    public static void setSkinType(SkinType type) {
        skinType = type;
    }

    public static TriggerMode getTriggerMode() {
        return triggerMode;
    }

    public static void setTriggerMode(TriggerMode mode) {
        triggerMode = mode;
    }

    public static boolean getShowNotifications() {
        return showNotifications;
    }

    public static void setShowNotifications(boolean value) {
        showNotifications = value;
    }

    public static long getStillThresholdMs() {
        return stillThresholdMs;
    }

    public static void setStillThresholdMs(long value) {
        stillThresholdMs = value;
    }

    public static String getSkinUrl(String key) {
        return skinUrls.getOrDefault(key, "");
    }

    public static void setSkinUrl(String key, String url) {
        skinUrls.put(key, url);
    }

    public static void reset() {
        skinType = SkinType.CLASSIC;
        triggerMode = TriggerMode.ENTER;
        showNotifications = true;
        stillThresholdMs = 5000;
        skinUrls.clear();
    }

    public static Map<String, String> getAllSkinUrls() {
        return new HashMap<>(skinUrls);
    }

    // Internal config structure
    private static class SavedConfig {
        String skinType;
        String triggerMode;
        boolean showNotifications;
        long stillThresholdMs;
        HashMap<String, String> skinUrls;
    }
}