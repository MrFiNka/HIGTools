package me.redcarlos.higtools.modules.highwayborers;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.stream.IntStream;

import static me.redcarlos.higtools.utils.HIGUtils.*;

public abstract class BorerModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    protected final Setting<PosNegBorer.Shape> mode = sgGeneral.add(new EnumSetting.Builder<PosNegBorer.Shape>()
        .name("shape")
        .description("Which shape to dig.")
        .defaultValue(Shape.HIGHWAY)
        .build()
    );

    protected final Setting<Integer> extForward;
    protected final Setting<Integer> extBackward;
    protected final Setting<Integer> xOffset;
    protected final Setting<Integer> zOffset;

    protected final Setting<Integer> keepY = sgGeneral.add(new IntSetting.Builder()
        .name("keepY")
        .description("Keeps a specific Y level when digging.")
        .defaultValue(119)
        .range(-1, 255)
        .sliderRange(-1, 255)
        .build()
    );

    protected int packets = 0;
    protected long lastUpdateTime = 0; // Last time packets were sent
    protected BlockPos playerPos = BlockPos.ZERO; // Floored block position of player

    protected BorerModule(String name, String description, int extForwards, int extBackwards, int xOffset, int zOffset) {
        super(HIGTools.BORERS, name, description);

        extForward = sgGeneral.add(new IntSetting.Builder()
            .name("ext-forward")
            .description("How many blocks to dig forwards.")
            .defaultValue(extForwards)
            .range(1, 6)
            .build()
        );

        extBackward = sgGeneral.add(new IntSetting.Builder()
            .name("ext-backward")
            .description("How many blocks to dig backwards.")
            .defaultValue(extBackwards)
            .range(1, 6)
            .build()
        );

        this.xOffset = sgGeneral.add(new IntSetting.Builder()
            .name("X-offset")
            .description("How many blocks to dig on X axis.")
            .defaultValue(xOffset)
            .range(-2, 2)
            .sliderRange(-2, 2)
            .build()
        );

        this.zOffset = sgGeneral.add(new IntSetting.Builder()
            .name("Z-offset")
            .description("How many blocks to dig on Z axis.")
            .defaultValue(zOffset)
            .range(-2, 2)
            .sliderRange(-2, 2)
            .build()
        );
    }

    @EventHandler
    public abstract void onTick(TickEvent.Pre event);

    protected void do2x3(BlockPos playerPos) {
        IntStream.rangeClosed(-extBackward.get(), extForward.get()).forEach(i -> {
            breakBlock(forward(playerPos, i));
            breakBlock(forward(playerPos, i).above());
            breakBlock(forward(playerPos, i).above(2));
            breakBlock(left(forward(playerPos, i), 1));
            breakBlock(left(forward(playerPos, i), 1).above());
            breakBlock(left(forward(playerPos, i), 1).above(2));
        });
    }

    protected void doHighway4(BlockPos playerPos) {
        IntStream.rangeClosed(-extBackward.get(), extForward.get()).forEach(i -> {
            breakBlock(forward(playerPos, i));
            breakBlock(forward(playerPos, i).above());
            breakBlock(forward(playerPos, i).above(2));
            breakBlock(forward(playerPos, i).above(3));

            breakBlock(right(forward(playerPos, i), 1));
            breakBlock(right(forward(playerPos, i), 1).above());
            breakBlock(right(forward(playerPos, i), 1).above(2));
            breakBlock(right(forward(playerPos, i), 1).above(3));

            breakBlock(right(forward(playerPos, i), 2).above());
            breakBlock(right(forward(playerPos, i), 2).above(2));
            breakBlock(right(forward(playerPos, i), 2).above(3));

            breakBlock(left(forward(playerPos, i), 1));
            breakBlock(left(forward(playerPos, i), 1).above());
            breakBlock(left(forward(playerPos, i), 1).above(2));
            breakBlock(left(forward(playerPos, i), 1).above(3));

            breakBlock(left(forward(playerPos, i), 2));
            breakBlock(left(forward(playerPos, i), 2).above());
            breakBlock(left(forward(playerPos, i), 2).above(2));
            breakBlock(left(forward(playerPos, i), 2).above(3));

            breakBlock(left(forward(playerPos, i), 3).above());
            breakBlock(left(forward(playerPos, i), 3).above(2));
            breakBlock(left(forward(playerPos, i), 3).above(3));
        });
    }

    protected void breakBlock(BlockPos blockPos) {
        if (mc.player == null || mc.level == null) return;

        if (packets >= 130 || mc.level.getBlockState(blockPos).canBeReplaced()) {
            return;
        }

        mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.UP));
        mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));
        packets += 2;

        mc.player.getInventory().tick();
    }

    public enum Shape {
        THIN,
        HIGHWAY
    }
}
