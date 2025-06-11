package me.eldodebug.soar.injection.mixin.mixins.hackerdetector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector.*;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.handler.EventHandler;
import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class HackerDetectorClient {

    private static HackerDetectorClient instance;
    private final EventHandler eventHandler;
    private final Map<String, AbstractDetector> detectors;
    private final Set<String> flaggedPlayers;
    private final Map<String, Long> reportCooldowns;
    HackerDetectorMod mod = HackerDetectorMod.getInstance();
    public static final Logger LOGGER = LogManager.getLogger("HackerDetector");

    public HackerDetectorClient() {
        instance = this;
        this.eventHandler = new EventHandler(this);
        this.detectors = new HashMap<>();
        this.flaggedPlayers = new HashSet<>();
        this.reportCooldowns = new HashMap<>();

        initializeDetectors();
    }

    public static HackerDetectorClient getInstance() {
        if (instance == null) {
            instance = new HackerDetectorClient();
        }
        return instance;
    }

    private void initializeDetectors() {
        detectors.put("killaura", new KillAuraDetector());
        detectors.put("autoblock", new AutoBlockDetector());
        detectors.put("fastbreak", new FastBreakDetector());
        detectors.put("keepsprint", new KeepSprintDetector());
        detectors.put("noslowdown", new NoSlowdownDetector());
        detectors.put("scaffold", new ScaffoldDetector());
        detectors.put("ghosthand", new GhostHandDetector());
    }

    public void flagPlayer(EntityPlayer player, String cheatType, String reason) {
        if (player == null || cheatType == null) return;

        String playerName = player.getName();
        String flagKey = playerName + ":" + cheatType;

        if (flaggedPlayers.contains(flagKey)) return;

        flaggedPlayers.add(flagKey);

        if (mod.showFlagMessage.isToggled()) {
            String message = mod.compactFlagMessages.isToggled() ?
                    String.format("§c[HD] %s - %s", playerName, cheatType) :
                    String.format("§c[HD] %s detected using %s: %s", playerName, cheatType, reason);

            ChatUtils.addChatMessage(message);
        }

        if (mod.playFlagSound.isToggled()) {
            String soundName = getSoundName(mod.flagSoundType.getName());
            float volume = (float) mod.flagSoundVolume.getValue();
            float pitch = (float) mod.flagSoundPitch.getValue();

            Minecraft.getMinecraft().thePlayer.playSound(soundName, volume, pitch);
        }

        if (mod.autoReport.isToggled() && canReport(playerName)) {
            autoReport(playerName, cheatType);
        }

        LOGGER.info("Flagged player {} for {}: {}", playerName, cheatType, reason);
    }

    private String getSoundName(String soundType) {
        switch (soundType.toLowerCase()) {
            case "orb": return "random.orb";
            case "ding": return "random.successful_hit";
            case "anvil": return "random.anvil_use";
            case "bell": return "note.bell";
            case "note": return "note.pling";
            default: return "random.orb";
        }
    }

    private boolean canReport(String playerName) {
        long currentTime = System.currentTimeMillis();
        Long lastReport = reportCooldowns.get(playerName);

        if (lastReport == null) return true;

        long cooldownMs = (long) (mod.reportCooldown.getValue() * 1000);
        return currentTime - lastReport > cooldownMs;
    }

    private void autoReport(String playerName, String cheatType) {
        if (!mod.autoReport.isToggled()) return;

        String command = "/wdr " + playerName + " " + getReportCategory(cheatType);
        Minecraft.getMinecraft().thePlayer.sendChatMessage(command);

        reportCooldowns.put(playerName, System.currentTimeMillis());

        if (mod.showFlagMessage.isToggled()) {
            ChatUtils.addChatMessage("§a[HD] Auto-reported " + playerName + " for " + cheatType);
        }
    }

    private String getReportCategory(String cheatType) {
        switch (cheatType.toLowerCase()) {
            case "killaura":
            case "autoblock":
                return "killaura";
            case "fastbreak":
                return "fastbreak";
            case "scaffold":
                return "scaffold";
            default:
                return "cheating";
        }
    }


    public void processPlayerUpdate(EntityPlayer player) {
        if (player == null || player == Minecraft.getMinecraft().thePlayer) return;

        for (AbstractDetector detector : detectors.values()) {
            if (detector.isEnabled()) {
                detector.checkPlayer(player);
            }
        }
    }

    public Map<String, AbstractDetector> getDetectors() {
        return detectors;
    }

    public EventHandler getEventHandler() {
        return eventHandler;
    }
}