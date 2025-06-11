package me.eldodebug.soar.injection.mixin.mixins.hackerdetector.detector;

import me.eldodebug.soar.injection.mixin.mixins.hackerdetector.mod.HackerDetectorMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class ScaffoldDetector extends AbstractDetector {
    private final Map<String, Integer> rapidPlaceCount = new HashMap<>();
    private final Map<String, BlockPos> lastPlacedBlock = new HashMap<>();
    HackerDetectorMod mod = HackerDetectorMod.getInstance();
    public ScaffoldDetector() {
        super("Scaffold");
    }

    @Override
    public boolean isEnabled() {
        return mod.scaffold.isToggled();
    }

    @Override
    public void checkPlayer(EntityPlayer player) {
        // This would need to be hooked into block placement events
        checkPlacementPattern(player);
    }

    private void checkPlacementPattern(EntityPlayer player) {
        String name = player.getName();
        BlockPos playerPos = new BlockPos(player.posX, player.posY - 1, player.posZ);

        if (lastPlacedBlock.containsKey(name)) {
            BlockPos lastPos = lastPlacedBlock.get(name);

            // Check if placing blocks in a line while moving
            if (isScaffoldPattern(lastPos, playerPos) && player.motionX != 0 || player.motionZ != 0) {
                int count = rapidPlaceCount.getOrDefault(name, 0) + 1;
                rapidPlaceCount.put(name, count);

                if (count >= 5) {
                    flag(player, "Scaffold block placement pattern");
                    rapidPlaceCount.put(name, 0);
                }
            }
        }
    }

    private boolean isScaffoldPattern(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getZ() - pos2.getZ()) == 1 &&
                pos1.getY() == pos2.getY();
    }

    public void onBlockPlace(EntityPlayer player, BlockPos pos) {
        lastPlacedBlock.put(player.getName(), pos);
    }
}

