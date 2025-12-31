package net.treekytree.treeskin.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SkinMessageMixin {

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void interceptSkinMessages(GameMessageS2CPacket packet, CallbackInfo ci) {
        String raw = packet.content().getString();
        if (raw.contains("Skin changed") || raw.contains("Loading skin")|| raw.contains("No skin changes")) {
            ci.cancel(); // suppress server messages
        }
    }
}