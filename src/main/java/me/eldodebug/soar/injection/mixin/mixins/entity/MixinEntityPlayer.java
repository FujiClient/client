package me.eldodebug.soar.injection.mixin.mixins.entity;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.eldodebug.soar.injection.interfaces.IMixinEntityPlayer;
import me.eldodebug.soar.management.event.impl.EventAttackEntity;
import me.eldodebug.soar.management.event.impl.EventJump;
import me.eldodebug.soar.management.mods.impl.skin3d.render.CustomizableModelPart;
import me.eldodebug.soar.management.mods.impl.waveycapes.sim.StickSimulation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Mixin(EntityPlayer.class)
public class MixinEntityPlayer implements IMixinEntityPlayer {



	private CustomizableModelPart headLayer;
	private CustomizableModelPart[] skinLayer;

	private StickSimulation stickSimulation = new StickSimulation();

	@Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"))
	public void onAttackTargetEntityWithCurrentItem(Entity entity, CallbackInfo ci) {
		// 攻撃イベントを呼ぶだけで、canAttackWithItem()チェックやキャンセルはしない
		new EventAttackEntity(entity).call();
	}

	@Inject(method = "jump", at = @At("HEAD"))
	public void onJump(CallbackInfo ci) {
		new EventJump().call();
	}

	@Inject(method = "onUpdate", at = @At("HEAD"))
	private void onUpdateHead(CallbackInfo ci) {
		EntityPlayer player = (EntityPlayer)(Object)this;

		// マントやスキンのシミュレーション
		simulate(player);

		// 自分自身は除外
		if (player == Minecraft.getMinecraft().thePlayer) return;

		try {
			checkForHacks(player);
		} catch (Exception e) {
			// エラーは無視（ログを出さない）
		}
	}

	@Override
	public StickSimulation getSimulation() {
		return stickSimulation;
	}

	@Override
	public CustomizableModelPart[] getSkinLayers() {
		return skinLayer;
	}

	@Override
	public void setupSkinLayers(CustomizableModelPart[] box) {
		this.skinLayer = box;
	}

	@Override
	public CustomizableModelPart getHeadLayers() {
		return headLayer;
	}

	@Override
	public void setupHeadLayers(CustomizableModelPart box) {
		this.headLayer = box;
	}

	// 静的フィールドでデータを管理
	private static final Set<String> flaggedPlayers = new HashSet<>();
	private static final Map<String, Long> reportCooldowns = new HashMap<>();
	private static final Map<String, PlayerData> playerDataMap = new HashMap<>();

	private void checkForHacks(EntityPlayer player) {
		String playerName = player.getName();
		PlayerData data = playerDataMap.computeIfAbsent(playerName, k -> new PlayerData());

		// KillAura検出
		checkKillAura(player, data);

		// Movement検出
		checkMovement(player, data);

		// データ更新
		data.lastX = player.posX;
		data.lastY = player.posY;
		data.lastZ = player.posZ;
		data.lastMoveTime = System.currentTimeMillis();
	}

	private void checkKillAura(EntityPlayer player, PlayerData data) {
		// KillAura検出ロジック（簡略化）
		// 実際の検出ロジックをここに実装
	}

	private void checkMovement(EntityPlayer player, PlayerData data) {
		// Movement検出ロジック（簡略化）
		// 実際の検出ロジックをここに実装
	}

	private void flagPlayer(String playerName, String cheatType, String reason) {
		String flagKey = playerName + ":" + cheatType;

		if (flaggedPlayers.contains(flagKey)) return;
		flaggedPlayers.add(flagKey);

		// チャットメッセージ表示
		try {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.thePlayer != null) {
				String message = String.format("§c[HD] %s detected using %s", playerName, cheatType);
				mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(message));
			}
		} catch (Exception e) {
			// エラーは無視
		}
	}
}