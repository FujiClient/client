package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class FastBreakDetector extends AbstractDetector {
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

    @Override
    public void checkPlayer(EntityPlayer player) {
        // This would need to be hooked into block breaking events
        // For now, this is a placeholder implementation
        checkBreakingSpeed(player);
    }

    private void checkBreakingSpeed(EntityPlayer player) {
        String name = player.getName();
        long currentTime = System.currentTimeMillis();

        if (!lastResetTime.containsKey(name)) {
            lastResetTime.put(name, currentTime);
            blocksBroken.put(name, 0);
            return;
        }

        long timeDiff = currentTime - lastResetTime.get(name);

        // Reset counter every second
        if (timeDiff >= 1000) {
            int broken = blocksBroken.getOrDefault(name, 0);

            if (broken > mod.fastBreakThreshold.getValue()) {
                flag(player, "Breaking " + broken + " blocks per second");
            }

            blocksBroken.put(name, 0);
            lastResetTime.put(name, currentTime);
        }
    }

    public void onBlockBreak(EntityPlayer player) {
        String name = player.getName();
        int current = blocksBroken.getOrDefault(name, 0);
        blocksBroken.put(name, current + 1);
    }
}
