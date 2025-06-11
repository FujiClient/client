package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import net.minecraft.entity.player.EntityPlayer;

public abstract class AbstractDetector {
    protected final HackerDetectorClient client;
    protected final String detectorName;

    public AbstractDetector(String name) {
        this.client = HackerDetectorClient.getInstance();
        this.detectorName = name;
    }

    public abstract boolean isEnabled();
    public abstract void checkPlayer(EntityPlayer player);

    protected void flag(EntityPlayer player, String reason) {
        client.flagPlayer(player, detectorName, reason);
    }

    public String getName() {
        return detectorName;
    }
}
