package me.redcarlos.higtools.modules.highwayborers;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class PosPosBorer extends BorerModule {
    public PosPosBorer() {
        super("PosPos-borer", "Digs +X +Z highway.", 1, 2, 2, 2);
    }

    @Override
    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.level == null) return;

        // Previous floored block position of player
        BlockPos prevBlockPos = playerPos;

        playerPos = new BlockPos(
            Mth.floor(mc.player.getX()),
            keepY.get() != -1 ? keepY.get() : Mth.floor(mc.player.getY()),
            Mth.floor(mc.player.getZ())
        );

        if (playerPos != prevBlockPos || Util.getMillis() - lastUpdateTime > 800) {
            switch (mode.get()) {
                case THIN -> {
                    do2x3(playerPos.offset(xOffset.get(), 0, zOffset.get()));
                    do2x3(playerPos.offset(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
                case HIGHWAY -> {
                    doHighway4(playerPos.offset(xOffset.get(), 0, zOffset.get()));
                    doHighway4(playerPos.offset(xOffset.get() * -3, 0, zOffset.get() * -3));
                }
            }
            lastUpdateTime = Util.getMillis();
        }
        packets = 0;
    }
}
