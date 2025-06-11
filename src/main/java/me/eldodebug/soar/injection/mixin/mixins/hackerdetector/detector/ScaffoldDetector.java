package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class ScaffoldDetector extends AbstractDetector {
    public ScaffoldDetector() {
        super("Scaffold");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        // Mixinに移行
    }

    public void onBlockPlace(EntityPlayer player, BlockPos pos) {
        // Mixinに移行
    }
}

