package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import net.minecraft.entity.player.EntityPlayer;

public class KeepSprintDetector extends AbstractDetector {
    public KeepSprintDetector() {
        super("KeepSprint");
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

