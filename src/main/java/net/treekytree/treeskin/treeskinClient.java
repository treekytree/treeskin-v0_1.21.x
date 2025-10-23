package net.treekytree.treeskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class treeskinClient implements ClientModInitializer {
    private String lastState = "";
    private long lastSent = 0L;
    private static final long COOLDOWN_MS = 3000L;

    @Override
    public void onInitializeClient() {
        // Load config and commands
        TreeskinConfig.load();
        TreeskinCommands.register();

        // Main tick event: detects environment and applies correct skin
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            String state = detectDimension(client);
            long now = System.currentTimeMillis();

            if (!state.equals(lastState) && (now - lastSent >= COOLDOWN_MS)) {
                lastState = state;
                lastSent = now;

                String url = TreeskinConfig.getSkinUrl(state);
                if (url != null && !url.isEmpty()) {
                    sendSkinChangeCommand(client, url);
                    client.player.sendMessage(Text.literal("[TreeSkin] Switched skin for: " + state), false);
                } else {
                    client.player.sendMessage(Text.literal("[TreeSkin] No skin configured for: " + state), false);
                }
            }
        });
    }

    private String detectDimension(MinecraftClient client) {
        if (client.world.getRegistryKey() == World.NETHER) return "nether";
        if (client.world.getRegistryKey() == World.END) return "end";

        Biome biome = client.world.getBiome(client.player.getBlockPos()).value();
        float temp = biome.getTemperature();

        if (temp <= 0.3f) return "overworld_cold";
        if (temp >= 0.9f) return "overworld_hot";
        return "overworld_moderate";
    }

    private void sendSkinChangeCommand(MinecraftClient client, String url) {
        if (client.getNetworkHandler() == null) return;
        String cmd = "skin set web slim \"" + url + "\"";
        client.getNetworkHandler().sendChatCommand(cmd);
    }
}
