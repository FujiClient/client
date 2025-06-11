package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.impl.InventoryMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.client.gui.GuiButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class HackerDetectorMod extends Mod {

    private static HackerDetectorMod instance;

    public BooleanSetting killAura = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting autoBlock = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    private BooleanSetting fashBreak = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting keepSprint = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting noSlowdown = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting scaffold = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting ghostHand = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    public BooleanSetting fastBreak = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    private BooleanSetting flagMessage = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);
    private BooleanSetting flagSound = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);

    // Alert Settings
    public BooleanSetting showFlagMessage = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);;
    public BooleanSetting playFlagSound = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);;
    public BooleanSetting compactFlagMessages = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);;
    private BooleanSetting singleFlagPerGame = new BooleanSetting(TranslateText.TEXT_1_KEY, this, true);;

    // Auto-report Settings
    public BooleanSetting autoReport = new BooleanSetting(TranslateText.TEXT_1_KEY, this, false);;
    public NumberSetting reportCooldown = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 60, true);;

    // Detection Thresholds
    public NumberSetting killAuraMaxAngle = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;
    public NumberSetting keepSprintThreshold = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;
    public NumberSetting fastBreakThreshold = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;
    public NumberSetting noSlowdownThreshold = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;

    // Sound Settings
    public ComboSetting flagSoundType = new ComboSetting(TranslateText.TYPE, this,TranslateText.RIGHT, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.RIGHT), new Option(TranslateText.LEFT))));;
    public NumberSetting flagSoundVolume = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;
    public NumberSetting flagSoundPitch = new NumberSetting(TranslateText.MULTIPLIER, this, 2, 1, 64, true);;

    public HackerDetectorMod() {
        super(TranslateText.AUTO_TEXT, TranslateText.AUTO_TEXT_DESCRIPTION, ModCategory.PLAYER,"messagetexthotkeymacro");
    }

    public static HackerDetectorMod getInstance() {
        return instance;
    }

}
