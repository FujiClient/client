package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class KeepSprintDetector extends AbstractDetector {
    private final Map<String, Double> lastSpeed = new HashMap<>();
    private final Map<String, Integer> keepSprintCount = new HashMap<>();
    HackerDetectorMod mod = HackerDetectorMod.getInstance();

    public KeepSprintDetector() {
        super("KeepSprint");
    }

    @Override
    public boolean isEnabled() {
        return mod.keepSprint.isToggled();
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        if (!player.isSprinting()) return;

        String name = player.getName();
        double currentSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);

        if (lastSpeed.containsKey(name) && player.isSwingInProgress) {
            double speedRatio = currentSpeed / lastSpeed.get(name);

            // Normal sprint should slow down when attacking
            if (speedRatio > mod.keepSprintThreshold.getValue()) {
                int count = keepSprintCount.getOrDefault(name, 0) + 1;
                keepSprintCount.put(name, count);

                if (count >= 3) {
                    flag(player, "Maintaining sprint speed while attacking");
                    keepSprintCount.put(name, 0);
                }
            }
        }

        lastSpeed.put(name, currentSpeed);
    }
}

