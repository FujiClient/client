package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import net.minecraft.entity.player.EntityPlayer;

public class NoSlowdownDetector extends AbstractDetector {
    public NoSlowdownDetector() {
        super("NoSlowdown");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        // Mixinに移行
    }
}