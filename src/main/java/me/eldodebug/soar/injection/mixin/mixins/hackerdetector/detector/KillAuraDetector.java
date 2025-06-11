package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class KillAuraDetector extends AbstractDetector {
    private final Map<String, Float> lastYaw = new HashMap<>();
    private final Map<String, Float> lastPitch = new HashMap<>();
    private final Map<String, Integer> suspiciousRotations = new HashMap<>();
    HackerDetectorMod mod = HackerDetectorMod.getInstance();

    public KillAuraDetector() {
        super("KillAura");
    }

    @Override
    public boolean isEnabled() {
        return mod.killAura.isToggled();
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        if (player.isSwingInProgress) {
            checkRotationSpeed(player);
            checkRotationPattern(player);
        }
    }

    private void checkRotationSpeed(EntityPlayer player) {
        String name = player.getName();
        float currentYaw = player.rotationYaw;
        float currentPitch = player.rotationPitch;

        if (lastYaw.containsKey(name)) {
            float yawDiff = Math.abs(currentYaw - lastYaw.get(name));
            float pitchDiff = Math.abs(currentPitch - lastPitch.get(name));

            // Normalize yaw difference
            if (yawDiff > 180) {
                yawDiff = 360 - yawDiff;
            }

            // Check for impossible rotation speeds (> 90 degrees per tick)
            if (yawDiff > 90 || pitchDiff > 90) {
                int count = suspiciousRotations.getOrDefault(name, 0) + 1;
                suspiciousRotations.put(name, count);

                if (count >= 3) {
                    flag(player, "Impossible rotation speed: " + String.format("%.1f°/tick", Math.max(yawDiff, pitchDiff)));
                    suspiciousRotations.put(name, 0);
                }
            }
        }

        lastYaw.put(name, currentYaw);
        lastPitch.put(name, currentPitch);
    }

    private void checkRotationPattern(EntityPlayer player) {
        // Check if player is looking at target while attacking
        EntityPlayer target = getAttackTarget(player);
        if (target == null) return;

        double[] requiredLook = getRequiredLookAngles(player, target);
        double yawDiff = Math.abs(player.rotationYaw - requiredLook[0]);
        double pitchDiff = Math.abs(player.rotationPitch - requiredLook[1]);

        if (yawDiff > mod.killAuraMaxAngle.getValue() || pitchDiff > mod.killAuraMaxAngle.getValue()) {
            flag(player, "Attacking without looking at target");
        }
    }

    private EntityPlayer getAttackTarget(EntityPlayer attacker) {
        // Check recently attacked players
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (player != attacker && player.hurtTime > 0 &&
                    attacker.getDistanceToEntity(player) < 4.5) {
                return player;
            }
        }
        return null;
    }

    private double[] getRequiredLookAngles(EntityPlayer from, EntityPlayer to) {
        double dx = to.posX - from.posX;
        double dy = to.posY + to.getEyeHeight() - (from.posY + from.getEyeHeight());
        double dz = to.posZ - from.posZ;

        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        double pitch = Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        return new double[]{yaw, pitch};
    }
}