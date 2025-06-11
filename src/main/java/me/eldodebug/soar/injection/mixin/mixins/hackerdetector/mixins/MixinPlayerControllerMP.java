package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mixins;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.FastBreakDetector;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.ScaffoldDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    private static final Map<String, Long> lastBlockBreakTimes = new HashMap<>();

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        try {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            if (player != null) {
                // クリエイティブはbypass
                if (player.capabilities != null && player.capabilities.isCreativeMode) {
                    return;
                }
                // HackerDetectorClient logic
                HackerDetectorClient client = HackerDetectorClient.getInstance();
                if (client != null) {
                    FastBreakDetector detector = (FastBreakDetector) client.getDetectors().get("fastbreak");
                    if (detector != null) {
                        detector.onBlockBreak(player);
                    }
                }

                // Fast break detection logic
                checkFastBreak(player);
            }
        } catch (Exception e) {
            // Handle errors silently
        }
    }

    private void checkFastBreak(EntityPlayer player) {
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        Long lastBreak = lastBlockBreakTimes.get(playerName);

        if (lastBreak != null) {
            long timeDiff = currentTime - lastBreak;
            if (timeDiff < 50) { // Suspicious if less than 50ms
                flagFastBreak(playerName);
            }
        }

        lastBlockBreakTimes.put(playerName, currentTime);
    }

    private void flagFastBreak(String playerName) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                String message = String.format("§c[HD] %s detected using FastBreak", playerName);
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(message));
            }
        } catch (Exception e) {
            // Handle errors silently
        }
    }
}
