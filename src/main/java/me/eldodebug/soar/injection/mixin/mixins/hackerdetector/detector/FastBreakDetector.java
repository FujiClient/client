package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public abstract class FastBreakDetector extends AbstractDetector {
    private final Map<String, Integer> blocksBroken = new HashMap<>();
    private final Map<String, Long> lastResetTime = new HashMap<>();

    HackerDetectorMod mod = HackerDetectorMod.getInstance();

    public FastBreakDetector() {
        super("FastBreak");
    }

    @Override
    public boolean isEnabled() {
        return mod.fastBreak.isToggled();
    }


    public void onBlockBreak(EntityPlayer player) {
        String name = player.getName();
        int current = blocksBroken.getOrDefault(name, 0);
        blocksBroken.put(name, current + 1);
    }
}
