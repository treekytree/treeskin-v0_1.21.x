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
    private final long COOLDOWN_MS = 3000L;

    @Override
    public void onInitializeClient() {
        TreeskinConfig.load();
        TreeskinCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            String state = detectDimension(client);
            long now = System.currentTimeMillis();

            if (!state.equals(lastState) && (now - lastSent >= COOLDOWN_MS)) {
                lastState = state;
                lastSent = now;

                String url = TreeskinConfig.getSkinUrl(state.toLowerCase().replace(" ", "_"));
                if (url != null) {
                    String type = TreeskinConfig.getSkinType();
                    String cmd = "skin set web " + type + " \"" + url + "\"";
                    client.getNetworkHandler().sendChatCommand(cmd);
                    client.player.sendMessage(Text.literal("§a[TreeSkin] Switched skin for: " + state), true); // action bar
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
}
