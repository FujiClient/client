package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import net.minecraft.entity.player.EntityPlayer;

public class AutoBlockDetector extends AbstractDetector {
    public AutoBlockDetector() {
        super("AutoBlock");
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

