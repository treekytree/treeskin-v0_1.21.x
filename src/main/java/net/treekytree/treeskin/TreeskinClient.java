package net.treekytree.treeskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class TreeskinClient implements ClientModInitializer {

    private static final long ENTER_COOLDOWN_MS = 3000L;

    private static String lastAppliedState = "";
    private static long lastSent = 0L;

    private BlockPos lastPos = BlockPos.ORIGIN;
    private long lastMovedAt = 0L;

    private boolean wasSleeping = false;

    private String currentState = "";
    private String lastBiomeKey = "";
    private String lastDimensionKey = "";

    @Override
    public void onInitializeClient() {
        TreeskinConfig.load();
        TreeskinCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null || client.world == null) return;

            long now = System.currentTimeMillis();

            BlockPos pos = client.player.getBlockPos();
            if (!pos.equals(lastPos)) {
                lastPos = pos;
                lastMovedAt = now;
            }

            boolean sleeping = client.player.isSleeping();

            boolean stateChanged = shouldRecalculateState(client);
            if (stateChanged) {
                currentState = detectState(client);

                boolean isDimInstant =
                        currentState.equals("nether") ||
                                currentState.equals("end") ||
                                lastAppliedState.equals("nether") ||
                                lastAppliedState.equals("end");

                if (isDimInstant && !currentState.equals(lastAppliedState)) {
                    applySkin(client, now);
                    wasSleeping = sleeping;
                    return;
                }
            }

            TriggerMode mode = TreeskinConfig.getTriggerMode();
            boolean shouldTrigger = switch (mode) {
                case ENTER -> shouldTriggerEnter(now);
                case SLEEP -> shouldTriggerSleep(sleeping);
                case STILL -> shouldTriggerStill(now);
            };

            if (shouldTrigger && !currentState.equals(lastAppliedState)) {
                applySkin(client, now);
            }

            wasSleeping = sleeping;
        });
    }

    private boolean shouldTriggerEnter(long now) {
        return now - lastSent >= ENTER_COOLDOWN_MS;
    }

    private boolean shouldTriggerSleep(boolean sleeping) {
        return sleeping && !wasSleeping;
    }

    private boolean shouldTriggerStill(long now) {
        return now - lastMovedAt >= TreeskinConfig.getStillThresholdMs();
    }

    private boolean shouldRecalculateState(MinecraftClient client) {
        String dimKey = (client.world != null && client.world.getRegistryKey() != null)
                ? client.world.getRegistryKey().getValue().toString()
                : "";
        String biomeKey = (client.world != null && client.player != null)
                ? client.world.getBiome(client.player.getBlockPos())
                .getKey().map(k -> k.getValue().toString()).orElse("")
                : "";

        boolean changed = false;

        if (!dimKey.equals(lastDimensionKey)) {
            lastDimensionKey = dimKey;
            changed = true;
        }

        if (!biomeKey.equals(lastBiomeKey)) {
            lastBiomeKey = biomeKey;
            changed = true;
        }

        return changed;
    }

    private String detectState(MinecraftClient client) {
        if (client.world == null || client.player == null) return "";

        if (client.world.getRegistryKey() == World.NETHER) return "nether";
        if (client.world.getRegistryKey() == World.END) return "end";

        Biome biome = client.world.getBiome(client.player.getBlockPos()).value();
        float temp = biome.getTemperature();

        if (temp <= 0.3f) return "overworld_cold";
        if (temp >= 0.9f) return "overworld_hot";
        return "overworld_moderate";
    }

    private void applySkin(MinecraftClient client, long now) {
        String normalized = currentState.toLowerCase().replace(" ", "_");
        String url = TreeskinConfig.getSkinUrl(normalized);

        if (url == null || client.getNetworkHandler() == null) return;

        SkinType type = TreeskinConfig.getSkinType();
        String cmd = "skin set web " + type.name().toLowerCase() + " \"" + url + "\"";

        client.getNetworkHandler().sendChatCommand(cmd);

        if (TreeskinConfig.getShowNotifications() && client.player != null) {
            client.player.sendMessage(Text.literal("§a[TreeSkin] Switched skin for: " + currentState), true);
        }

        lastAppliedState = currentState;
        lastSent = now;
    }

    public static void forceApply() {
        lastAppliedState = "";
    }
}
