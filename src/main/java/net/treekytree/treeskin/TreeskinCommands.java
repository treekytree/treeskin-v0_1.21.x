package net.treekytree.treeskin;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;

public class TreeskinCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("treeskin")
                    // /treeskin set <state> <url>
                    .then(ClientCommandManager.literal("set")
                            .then(ClientCommandManager.argument("state", StringArgumentType.string())
                                    .suggests((ctx, builder) -> {
                                        builder.suggest("nether");
                                        builder.suggest("end");
                                        builder.suggest("overworld_cold");
                                        builder.suggest("overworld_hot");
                                        builder.suggest("overworld_moderate");
                                        return builder.buildFuture();
                                    })
                                    .then(ClientCommandManager.argument("url", StringArgumentType.string())
                                            .executes(ctx -> {
                                                String state = StringArgumentType.getString(ctx, "state").toLowerCase();
                                                String url = StringArgumentType.getString(ctx, "url");

                                                TreeskinConfig.setSkinUrl(state, url);
                                                ctx.getSource().sendFeedback(
                                                        Text.literal("§a[TreeSkin] Set " + state + " → " + url)
                                                );
                                                return 1;
                                            }))))
                    // /treeskin type <classic|slim>
                    .then(ClientCommandManager.literal("type")
                            .then(ClientCommandManager.argument("type", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        builder.suggest("classic");
                                        builder.suggest("slim");
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> {
                                        String type = StringArgumentType.getString(ctx, "type");
                                        TreeskinConfig.setSkinType(type);
                                        ctx.getSource().sendFeedback(
                                                Text.literal("§a[TreeSkin] Set skin type to §e" + type)
                                        );
                                        return 1;
                                    })))
            );
        });
    }
}
