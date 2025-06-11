package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;


import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;



public class GhostHandDetector extends AbstractDetector {
    public GhostHandDetector() {
        super("GhostHand");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        // Mixinに移行
    }

    public void onPlayerInteract(EntityPlayer player, net.minecraft.util.BlockPos pos) {
        // Mixinに移行
    }
}
