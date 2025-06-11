package me.eldodebug.soar.injection.mixin.mixins.entity;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.AbstractDetector;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.AbstractDetector;
import me.eldodebug.soar.management.mods.impl.DamageTiltMod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public class MixinEntity {

	private final Map<String, Integer> blocksBroken = new HashMap<>();
	private final Map<String, Long> lastResetTime = new HashMap<>();
	HackerDetectorMod mod = HackerDetectorMod.getInstance();

    @Shadow 
    public boolean onGround;

    @Inject(method = "spawnRunningParticles", at = @At("HEAD"), cancellable = true)
    private void checkGroundState(CallbackInfo ci) {
        if (!this.onGround) {
        	ci.cancel();
        }
    }

	private void runHackerDetection(EntityPlayer player) {
		try {
			HackerDetectorClient client = HackerDetectorClient.getInstance();

			// detectorの処理を直接実行

			if (mod.fastBreak.isToggled()){
				for (AbstractDetector detector : client.getDetectors().values()) {
					if (detector.isEnabled()) {
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
								detector.flag(player, "Breaking " + broken + " blocks per second");
							}

							blocksBroken.put(name, 0);
							lastResetTime.put(name, currentTime);
						}
					}
				}
			}
		} catch(Exception e){
				// エラーログを出力
				System.err.println("HackerDetector error: " + e.getMessage());
		}
	}

	// --- Detector用状態管理 ---
	private final Map<String, Float> lastYaw = new HashMap<>();
	private final Map<String, Float> lastPitch = new HashMap<>();
	private final Map<String, Integer> suspiciousRotations = new HashMap<>();
	private final Map<String, Double> lastSprintSpeed = new HashMap<>();
	private final Map<String, Integer> keepSprintCount = new HashMap<>();
	private final Map<String, Double> normalSpeed = new HashMap<>();
	private final Map<String, Boolean> wasBlocking = new HashMap<>();
	private final Map<String, Long> lastAttackTime = new HashMap<>();
	private final Map<String, Integer> rapidPlaceCount = new HashMap<>();
	private final Map<String, net.minecraft.util.BlockPos> lastPlacedBlock = new HashMap<>();
	private final Map<String, Integer> ghostBlockCount = new HashMap<>();

	@Redirect(method = "getBrightnessForRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/BlockPos;)Z"))
    public boolean alwaysReturnTrue(World world, BlockPos pos) {
        return true;
    }
    
	@Inject(method = "setVelocity", at = @At("HEAD"))
    public void preSetVelocity(double x, double y, double z, CallbackInfo ci) {
		if(DamageTiltMod.getInstance().isToggled()) {
			if((Entity)(Object)this != null) {
				EntityPlayer player = Minecraft.getMinecraft().thePlayer;
				if(player != null && ((Entity)(Object)this).equals(player)) {
					
					float result = (float)(Math.atan2(player.motionZ - z, player.motionX - x) * (180D / Math.PI) - (double)player.rotationYaw);
					
					if(Float.isFinite(result)) {
						player.attackedAtYaw = result;
					}
				}
			}
		}
	}

	// --- KillAura Detector ---
	private void runKillAuraDetection(EntityPlayer player) {
		if (!mod.killAura.isToggled()) return;
		String name = player.getName();
		if (player.isSwingInProgress) {
			// 回転速度チェック
			float currentYaw = player.rotationYaw;
			float currentPitch = player.rotationPitch;
			if (lastYaw.containsKey(name)) {
				float yawDiff = Math.abs(currentYaw - lastYaw.get(name));
				float pitchDiff = Math.abs(currentPitch - lastPitch.get(name));
				if (yawDiff > 180) yawDiff = 360 - yawDiff;
				if (yawDiff > 90 || pitchDiff > 90) {
					int count = suspiciousRotations.getOrDefault(name, 0) + 1;
					suspiciousRotations.put(name, count);
					if (count >= 3) {
						flag(player, "KillAura", "Impossible rotation speed: " + String.format("%.1f°/tick", Math.max(yawDiff, pitchDiff)));
						suspiciousRotations.put(name, 0);
					}
				}
			}
			lastYaw.put(name, currentYaw);
			lastPitch.put(name, currentPitch);

			// ターゲット注視チェック
			EntityPlayer target = getAttackTarget(player);
			if (target != null) {
				double[] requiredLook = getRequiredLookAngles(player, target);
				double yawDiff = Math.abs(player.rotationYaw - requiredLook[0]);
				double pitchDiff = Math.abs(player.rotationPitch - requiredLook[1]);
				if (yawDiff > mod.killAuraMaxAngle.getValue() || pitchDiff > mod.killAuraMaxAngle.getValue()) {
					flag(player, "KillAura", "Attacking without looking at target");
				}
			}
		}
	}
	private EntityPlayer getAttackTarget(EntityPlayer attacker) {
		for (EntityPlayer p : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (p != attacker && p.hurtTime > 0 && attacker.getDistanceToEntity(p) < 4.5) {
				return p;
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

	// --- KeepSprint Detector ---
	private void runKeepSprintDetection(EntityPlayer player) {
		if (!mod.keepSprint.isToggled()) return;
		if (!player.isSprinting()) return;
		String name = player.getName();
		double currentSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
		if (lastSprintSpeed.containsKey(name) && player.isSwingInProgress) {
			double speedRatio = currentSpeed / lastSprintSpeed.get(name);
			if (speedRatio > mod.keepSprintThreshold.getValue()) {
				int count = keepSprintCount.getOrDefault(name, 0) + 1;
				keepSprintCount.put(name, count);
				if (count >= 3) {
					flag(player, "KeepSprint", "Maintaining sprint speed while attacking");
					keepSprintCount.put(name, 0);
				}
			}
		}
		lastSprintSpeed.put(name, currentSpeed);
	}

	// --- NoSlowdown Detector ---
	private void runNoSlowdownDetection(EntityPlayer player) {
		if (!mod.noSlowdown.isToggled()) return;
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
				flag(player, "NoSlowdown", "Moving too fast while using item");
			}
		}
	}

	// --- AutoBlock Detector ---
	private void runAutoBlockDetection(EntityPlayer player) {
		if (!mod.autoBlock.isToggled()) return;
		if (!(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemSword)) return;
		String name = player.getName();
		boolean currentlyBlocking = player.isBlocking();
		boolean currentlyAttacking = player.isSwingInProgress;
		if (currentlyBlocking && currentlyAttacking) {
			flag(player, "AutoBlock", "Blocking while attacking");
		}
		if (wasBlocking.containsKey(name)) {
			boolean wasBlockingBefore = wasBlocking.get(name);
			Long lastAtk = lastAttackTime.get(name);
			if (!wasBlockingBefore && currentlyBlocking && lastAtk != null &&
					System.currentTimeMillis() - lastAtk < 50) {
				flag(player, "AutoBlock", "Instant block after attack");
			}
		}
		wasBlocking.put(name, currentlyBlocking);
		if (currentlyAttacking) {
			lastAttackTime.put(name, System.currentTimeMillis());
		}
	}

	// --- Scaffold Detector ---
	private void runScaffoldDetection(EntityPlayer player) {
		if (!mod.scaffold.isToggled()) return;
		String name = player.getName();
		BlockPos playerPos = new BlockPos(player.posX, player.posY - 1, player.posZ);
		if (lastPlacedBlock.containsKey(name)) {
			BlockPos lastPos = lastPlacedBlock.get(name);
			if (isScaffoldPattern(lastPos, playerPos) && (player.motionX != 0 || player.motionZ != 0)) {
				int count = rapidPlaceCount.getOrDefault(name, 0) + 1;
				rapidPlaceCount.put(name, count);
				if (count >= 5) {
					flag(player, "Scaffold", "Scaffold block placement pattern");
					rapidPlaceCount.put(name, 0);
				}
			}
		}
		// ScaffoldのlastPlacedBlock更新は本来BlockPlaceイベントで呼ぶべき
	}
	private boolean isScaffoldPattern(BlockPos pos1, BlockPos pos2) {
		return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getZ() - pos2.getZ()) == 1 &&
				pos1.getY() == pos2.getY();
	}
	// Scaffold用: 外部から呼び出す
	public void onBlockPlace(EntityPlayer player, BlockPos pos) {
		lastPlacedBlock.put(player.getName(), pos);
	}

	// --- GhostHand Detector ---
	private void runGhostHandDetection(EntityPlayer player) {
		if (!mod.ghostHand.isToggled()) return;
		// 実際の判定はonPlayerInteract等で呼ぶ必要あり
	}
	public void onPlayerInteract(EntityPlayer player, BlockPos pos) {
		if (isBlockObstructed(player, pos)) {
			String name = player.getName();
			int count = ghostBlockCount.getOrDefault(name, 0) + 1;
			ghostBlockCount.put(name, count);
			if (count >= 3) {
				flag(player, "GhostHand", "Interacting through blocks");
				ghostBlockCount.put(name, 0);
			}
		}
	}
	private boolean isBlockObstructed(EntityPlayer player, BlockPos pos) {
		// 実装は省略（常にfalse）
		return false;
	}

	// --- flag共通 ---
	private final Map<String, Long> lastFlagTime = new HashMap<>();
	private void flag(EntityPlayer player, String cheatType, String reason) {
		String key = player.getName() + ":" + cheatType;
		long now = System.currentTimeMillis();
		if (lastFlagTime.containsKey(key) && now - lastFlagTime.get(key) < 3000) return;
		lastFlagTime.put(key, now);
		try {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.thePlayer != null) {
				String message = String.format("§c[HD] %s detected using %s: %s", player.getName(), cheatType, reason);
				mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(message));
			}
		} catch (Exception ignored) {}
	}
}
