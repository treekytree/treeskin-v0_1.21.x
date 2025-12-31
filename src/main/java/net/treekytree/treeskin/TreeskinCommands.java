package net.treekytree.treeskin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class TreeskinCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> registerCommands(dispatcher));
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(literal("treeskin")

                .then(literal("set")
                        .then(argument("state", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("nether");
                                    builder.suggest("end");
                                    builder.suggest("overworld_cold");
                                    builder.suggest("overworld_hot");
                                    builder.suggest("overworld_moderate");
                                    return builder.buildFuture();
                                })
                                .then(argument("url", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String state = StringArgumentType.getString(ctx, "state");
                                            String url = StringArgumentType.getString(ctx, "url");
                                            TreeskinConfig.setSkinUrl(state, url);
                                            TreeskinConfig.save();
                                            send("Set " + state + " → " + url);
                                            return 1;
                                        })
                                )
                        )
                )

                .then(literal("type")
                        .then(argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("classic");
                                    builder.suggest("slim");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String input = StringArgumentType.getString(ctx, "type");
                                    try {
                                        SkinType type = SkinType.valueOf(input.toUpperCase());
                                        TreeskinConfig.setSkinType(type);
                                        TreeskinConfig.save();
                                        send("Skin type set to: " + type.name());
                                    } catch (IllegalArgumentException e) {
                                        send("Invalid skin type. Use: CLASSIC or SLIM");
                                    }
                                    return 1;
                                })
                        )
                )

                .then(literal("trigger")
                        .then(argument("mode", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("enter");
                                    builder.suggest("sleep");
                                    builder.suggest("still");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String input = StringArgumentType.getString(ctx, "mode");
                                    try {
                                        TriggerMode mode = TriggerMode.valueOf(input.toUpperCase());
                                        TreeskinConfig.setTriggerMode(mode);
                                        TreeskinConfig.save();
                                        send("Trigger mode set to: " + mode.name());
                                    } catch (IllegalArgumentException e) {
                                        send("Invalid trigger mode. Use: ENTER, SLEEP, or STILL");
                                    }
                                    return 1;
                                })
                        )
                )

                .then(literal("still")
                        .then(argument("ms", LongArgumentType.longArg(1000, 60000))
                                .executes(ctx -> {
                                    long ms = LongArgumentType.getLong(ctx, "ms");
                                    TreeskinConfig.setStillThresholdMs(ms);
                                    TreeskinConfig.save();
                                    send("Stillness threshold set to: " + ms + " ms");
                                    return 1;
                                })
                        )
                )

                .then(literal("notify")
                        .then(argument("value", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("on");
                                    builder.suggest("off");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String value = StringArgumentType.getString(ctx, "value");
                                    boolean enabled = value.equalsIgnoreCase("on");
                                    TreeskinConfig.setShowNotifications(enabled);
                                    TreeskinConfig.save();
                                    send("Notifications: " + (enabled ? "ON" : "OFF"));
                                    return 1;
                                })
                        )
                )

                .then(literal("info")
                        .executes(ctx -> {
                            send("TreeSkin Info:");
                            send("Trigger mode: " + TreeskinConfig.getTriggerMode());
                            send("Stillness threshold: " + TreeskinConfig.getStillThresholdMs() + " ms");
                            send("Skin type: " + TreeskinConfig.getSkinType());
                            send("Notifications: " + TreeskinConfig.getShowNotifications());
                            return 1;
                        })
                )

                .then(literal("list")
                        .executes(ctx -> {
                            send("Saved TreeSkin URLs:");
                            TreeskinConfig.getAllSkinUrls().forEach((key, value) ->
                                    send("§7" + key + " → §e" + ((value == null || value.isEmpty()) ? "<none>" : value))
                            );
                            return 1;
                        })
                )

                .then(literal("reload")
                        .executes(ctx -> {
                            TreeskinConfig.load();
                            send("Config reloaded from disk.");
                            return 1;
                        })
                )

                .then(literal("test")
                        .executes(ctx -> {
                            TreeskinClient.forceApply();
                            send("Forcing skin update...");
                            return 1;
                        })
                )

                .then(literal("reset")
                        .executes(ctx -> {
                            TreeskinConfig.reset();
                            TreeskinConfig.save();
                            send("Config reset to defaults.");
                            return 1;
                        })
                )
        );
    }

    private static void send(String msg) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a[TreeSkin] " + msg), false);
        }
    }
}