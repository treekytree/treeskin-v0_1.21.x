package net.treekytree.treeskin;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.minecraft.text.Text;

public class TreeskinCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("treeskin")
                    .then(ClientCommandManager.literal("set")
                            .then(ClientCommandManager.argument("state", StringArgumentType.string())
                                    .then(ClientCommandManager.argument("url", StringArgumentType.string())
                                            .executes(ctx -> {
                                                String state = StringArgumentType.getString(ctx, "state").toLowerCase();
                                                String url = StringArgumentType.getString(ctx, "url");

                                                TreeskinConfig.setSkinUrl(state, url);
                                                ctx.getSource().sendFeedback(
                                                        Text.literal("[TreeSkin] Set " + state + " → " + url)
                                                );
                                                return 1;
                                            }))))
            );
        });
    }
}
