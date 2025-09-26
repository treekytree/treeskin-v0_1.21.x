package net.treekytree.treeskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;


public class treeskinClient implements ClientModInitializer {
    private String lastState = "";
    private long lastSent = 0L;             // when last message was sent
    private final long COOLDOWN_MS = 3000L; // 3 seconds

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            String state = detectDimension(client);

            // only send if state changed AND cooldown passed
            long now = System.currentTimeMillis();
            if (!state.equals(lastState) && (now - lastSent >= COOLDOWN_MS)) {
                lastState = state;
                lastSent = now;
                client.player.sendMessage(Text.literal("You are now in: " + state), false);
            }
        });
    }

    private String detectDimension(MinecraftClient client) {
        if (client.world.getRegistryKey() == World.NETHER) return "Nether";
        if (client.world.getRegistryKey() == World.END) return "End";

        // Overworld → check biome base temperature
        Biome biome = client.world.getBiome(client.player.getBlockPos()).value();
        float temp = biome.getTemperature();

        if (temp <= 0.3f) return "Overworld (Cold)";
        if (temp >= 0.9f) return "Overworld (Hot)";
        return "Overworld (Moderate)";
    }
}
