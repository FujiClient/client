package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemBow;

import java.util.HashMap;
import java.util.Map;

public class NoSlowdownDetector extends AbstractDetector {
    private final Map<String, Double> normalSpeed = new HashMap<>();
    HackerDetectorMod mod = HackerDetectorMod.getInstance();
    public NoSlowdownDetector() {
        super("NoSlowdown");
    }

    @Override
    public boolean isEnabled() {
        return mod.noSlowdown.isToggled();
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        String name = player.getName();
        double currentSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);

        boolean shouldBeSlow = (player.isBlocking() || player.isUsingItem()) &&
                (player.getHeldItem() != null &&
                        (player.getHeldItem().getItem() instanceof ItemSword ||
                                player.getHeldItem().getItem() instanceof ItemBow));

        if (!shouldBeSlow) {
            normalSpeed.put(name, currentSpeed);
            return;
        }

        if (normalSpeed.containsKey(name) && normalSpeed.get(name) > 0) {
            double speedRatio = currentSpeed / normalSpeed.get(name);

            if (speedRatio > mod.noSlowdownThreshold.getValue()) {
                flag(player, "Moving too fast while using item");
            }
        }
    }
}