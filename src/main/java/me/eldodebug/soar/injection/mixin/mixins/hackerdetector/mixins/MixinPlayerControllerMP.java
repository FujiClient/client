package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mixins;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.FastBreakDetector;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.ScaffoldDetector;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"))
    private void onBlockBreak(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        HackerDetectorClient client = HackerDetectorClient.getInstance();
        if (client != null) {
            FastBreakDetector detector = (FastBreakDetector) client.getDetectors().get("fastbreak");
            if (detector != null) {
                EntityPlayerSP player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
                if (player != null) {
                    detector.onBlockBreak(player);
                }
            }
        }
    }

    @Inject(method = "onPlayerRightClick", at = @At("HEAD"))
    private void onPlayerRightClick(EntityPlayerSP player, WorldClient worldIn, ItemStack heldStack, BlockPos hitPos, EnumFacing side, Vec3 hitVec, CallbackInfoReturnable<Boolean> cir) {
        HackerDetectorClient client = HackerDetectorClient.getInstance();
        if (client != null && heldStack != null && heldStack.getItem() instanceof net.minecraft.item.ItemBlock) {
            ScaffoldDetector detector = (ScaffoldDetector) client.getDetectors().get("scaffold");
            if (detector != null && player != null && hitPos != null) {
                detector.onBlockPlace(player, hitPos);
            }
        }
    }
}
