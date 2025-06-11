package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class KillAuraDetector extends AbstractDetector {
    public KillAuraDetector() {
        super("KillAura");
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