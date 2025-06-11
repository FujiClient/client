package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mixins;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleChat", at = @At("HEAD"))
    private void onChatMessage(S02PacketChat packetIn, CallbackInfo ci) {
        String message = packetIn.getChatComponent().getUnformattedText();
    }
}