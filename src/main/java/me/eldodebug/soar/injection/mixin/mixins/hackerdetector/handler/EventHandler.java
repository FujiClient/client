package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.handler;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.HackerDetectorClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class EventHandler {
    private final HackerDetectorClient client;

    public EventHandler(HackerDetectorClient client) {
        this.client = client;
    }

    public void onPlayerTick(EntityPlayer player) {
        client.processPlayerUpdate(player);
    }

    public void onWorldTick() {
        Minecraft mc = Minecraft.getMinecraft();


        if (mc == null || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) {
                return; // ワールドやプレイヤーがまだ初期化されていない段階では何もしない
        }

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != mc.thePlayer) {
                onPlayerTick(player);
            }
        }
    }


}
