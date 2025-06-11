package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

import java.util.HashMap;
import java.util.Map;

public class AutoBlockDetector extends AbstractDetector {
    private final Map<String, Boolean> wasBlocking = new HashMap<>();
    private final Map<String, Long> lastAttackTime = new HashMap<>();
    HackerDetectorMod mod = HackerDetectorMod.getInstance();

    public AutoBlockDetector() {
        super("AutoBlock");
    }

    @Override
    public boolean isEnabled() {
        return mod.autoBlock.isToggled();
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        if (!(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemSword)) {
            return;
        }

        String name = player.getName();
        boolean currentlyBlocking = player.isBlocking();
        boolean currentlyAttacking = player.isSwingInProgress;

        // Check for simultaneous blocking and attacking
        if (currentlyBlocking && currentlyAttacking) {
            flag(player, "Blocking while attacking");
        }

        // Check for instant block after attack
        if (wasBlocking.containsKey(name)) {
            boolean wasBlockingBefore = wasBlocking.get(name);
            Long lastAttack = lastAttackTime.get(name);

            if (!wasBlockingBefore && currentlyBlocking && lastAttack != null &&
                    System.currentTimeMillis() - lastAttack < 50) { // Less than 50ms
                flag(player, "Instant block after attack");
            }
        }

        wasBlocking.put(name, currentlyBlocking);

        if (currentlyAttacking) {
            lastAttackTime.put(name, System.currentTimeMillis());
        }
    }
}

