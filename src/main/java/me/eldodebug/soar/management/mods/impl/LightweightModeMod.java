package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.world.World;

public class LightweightModeMod extends Mod {

    private final BooleanSetting disableRain = new BooleanSetting("Disable Rain/Snow", this, true);
    private final BooleanSetting disableClouds = new BooleanSetting("Disable Clouds", this, true);
    private final BooleanSetting disableParticles = new BooleanSetting("Disable Particles", this, false);
    private final BooleanSetting disableShadows = new BooleanSetting("Disable Shadows", this, false);
    private final BooleanSetting disableFog = new BooleanSetting("Disable Fog", this, false);

    public LightweightModeMod() {
        super("Lightweight Mode", "独自の軽量化: 雨・雲・パーティクル・影・フォグ等を個別に無効化", ModCategory.RENDER);
        this.addSettings(disableRain, disableClouds, disableParticles, disableShadows, disableFog);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        // 軽量化状態のHUD表示例
        StringBuilder sb = new StringBuilder("軽量化: ");
        if (disableRain.isToggled()) sb.append("雨☓ ");
        if (disableClouds.isToggled()) sb.append("雲☓ ");
        if (disableParticles.isToggled()) sb.append("粒☓ ");
        if (disableShadows.isToggled()) sb.append("影☓ ");
        if (disableFog.isToggled()) sb.append("霧☓ ");
        Minecraft.getMinecraft().fontRendererObj.drawString(sb.toString(), 5, 5, 0x88FFFFFF, true);
    }

    @Override
    public void onEnable() {
        applyLightweight();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        restoreVanilla();
        super.onDisable();
    }

    private void applyLightweight() {
        Minecraft mc = Minecraft.getMinecraft();
        if (disableRain.isToggled() && mc.theWorld != null) {
            mc.theWorld.setRainStrength(0.0F);
            mc.theWorld.setThunderStrength(0.0F);
        }
        if (disableClouds.isToggled()) {
            mc.gameSettings.clouds = 0; // OFF
        }
        if (disableParticles.isToggled() && mc.effectRenderer != null) {
            mc.effectRenderer.clearEffects(mc.theWorld);
        }
        if (disableShadows.isToggled()) {
            mc.gameSettings.ambientOcclusion = 0;
        }
        if (disableFog.isToggled()) {
            // フォグの強制無効化（簡易）
            System.setProperty("fml.fog", "false");
        }
    }

    private void restoreVanilla() {
        Minecraft mc = Minecraft.getMinecraft();
        // 雨・雷はワールドの天候に従う
        // 雲
        mc.gameSettings.clouds = 1;
        // パーティクル
        mc.gameSettings.particleSetting = 0;
        // 影
        mc.gameSettings.ambientOcclusion = 2;
        // フォグ
        System.clearProperty("fml.fog");
    }
}
