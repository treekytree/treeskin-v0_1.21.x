package net.treekytree.treeskin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.List;

public class TreeskinCommands {

    private static final SuggestionProvider<FabricClientCommandSource> STATE_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(
                    List.of("overworld_cold", "overworld_hot", "overworld_moderate", "nether", "end"),
                    builder
            );

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("treeskin")
                    .then(ClientCommandManager.literal("set")
                            .then(ClientCommandManager.argument("state", StringArgumentType.word())
                                    .suggests(STATE_SUGGESTIONS)
                                    .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                String state = StringArgumentType.getString(ctx, "state").toLowerCase();
                                                String url = StringArgumentType.getString(ctx, "url");

                                                TreeskinConfig.setSkinUrl(state, url);
                                                //|\ctx.getSource().sendFeedback(Text.literal("§a[TreeSkin] Set §e" + state + " §ato → §b" + url));
                                                return 1;
                                            }))))
            );
        });
    }
}
