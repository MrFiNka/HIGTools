package me.redcarlos.higtools.modules.main;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTotem;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;

public class OffhandManager extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoGap = settings.createGroup("Auto Gap");
    private final SettingGroup sgAutoTotem = settings.createGroup("Auto Totem");

    private final Setting<Boolean> hotbar = sgGeneral.add(new BoolSetting.Builder()
        .name("hotbar")
        .description("Whether to use items from your hotbar.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> warningMsg = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Warns you when the selected item is not found in your inventory.")
        .defaultValue(false)
        .build()
    );

    // Auto Gap

    private final Setting<Integer> hungerThreshold = sgAutoGap.add(new IntSetting.Builder()
        .name("hunger")
        .description("Hunger to gap at.")
        .defaultValue(16)
        .range(1, 19)
        .sliderRange(1, 19)
        .build()
    );

    private final Setting<Boolean> autoFireRes = sgAutoGap.add(new BoolSetting.Builder()
        .name("fire-resistance")
        .description("Gap when no fire resistance effect.")
        .defaultValue(true)
        .build()
    );

    // Auto Totem

    private final Setting<Integer> healthThreshold = sgAutoTotem.add(new IntSetting.Builder()
        .name("min-health")
        .description("The minimum health to hold a totem at.")
        .defaultValue(6)
        .range(0, 35)
        .sliderMax(35)
        .build()
    );

    private final Setting<Boolean> fallDamage = sgAutoTotem.add(new BoolSetting.Builder()
        .name("fall-damage")
        .description("Holds a totem when fall damage could kill you.")
        .defaultValue(true)
        .build()
    );

    private Item currentItem;
    private boolean eating;
    private boolean justStarted;
    private boolean moved;
    private boolean sentMsg;
    private boolean swapped;

    public OffhandManager() {
        super(HIGTools.MAIN, "offhand-manager", "Automatically manages your offhand (optimized for highway work).");
    }

    @Override
    public void onActivate() {
        eating = false;
        justStarted = true;
        moved = false;
        sentMsg = false;
        swapped = true;
        currentItem = Item.Totem;
    }

    @Override
    public void onDeactivate() {
        if (eating) stopEating();
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (!Utils.canUpdate()) return;

        // Anti cursor stack
        if (moved) {
            InvUtils.dropHand();
            moved = false;
        }

        Modules modules = Modules.get();
        AutoTotem autoTotem = modules.get(AutoTotem.class);
        if (Modules.get().get(ScaffoldHIG.class).hasWorked()) return;

        // Switch back if swap key has been used
        if (mc.options.keySwapOffhand.isDown() && !swapped && mc.player.getOffhandItem().getItem() == currentItem.item) swapped = true;
        else if (mc.options.keySwapOffhand.isDown() && swapped && mc.player.getMainHandItem().getItem() == currentItem.item) swapped = false;
        else swapped = false;

        // Checking offhand item
        if (mc.player.getOffhandItem().getItem() != currentItem.item && !swapped) {
            FindItemResult item = InvUtils.find(itemStack -> itemStack.getItem() == currentItem.item, hotbar.get() ? 0 : 9, 35);

            if (!item.found()) {
                if (!sentMsg) {
                    if (warningMsg.get()) warning("Chosen item not found.");
                    sentMsg = true;
                    moved = false;
                }
            }

            // Swap to offhand
            else if (!autoTotem.isLocked() && !item.isOffhand()) {
                InvUtils.move().from(item.slot()).toOffhand();
                sentMsg = false;
                moved = true;
            }
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        if (eating) {
            // If we are eating check if we should still be eating
            if (shouldEat()) {
                // Check if the item in current slot is not an egap
                if (mc.player.getOffhandItem().getComponents().get(DataComponents.FOOD) == null) {
                    stopEating();
                } else {
                    startEating();
                }
            } else {
                stopEating(); // If we shouldn't be eating anymore then stop
            }
        } else {
            // If we are not eating check if we should start eating
            if (shouldEat() && mc.player.getOffhandItem().getComponents().get(DataComponents.FOOD) != null) {
                startEating();
            }
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.getHealth() <= healthThreshold.get() || fallDamage.get() && !EntityUtils.isAboveWater(mc.player) && mc.player.fallDistance > 3) currentItem = Item.Totem;
        else if (autoFireRes.get() && !justStarted && !mc.player.getActiveEffectsMap().containsKey(MobEffects.FIRE_RESISTANCE)) currentItem = Item.EGap;
        else if (mc.player.getFoodData().getFoodLevel() < hungerThreshold.get() + 1) currentItem = Item.EGap;
        else currentItem = Item.Totem;
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof ServerboundUseItemOnPacket) stopEating();
    }

    private boolean shouldEat() {
        return (mc.player.getFoodData().getFoodLevel() <= hungerThreshold.get() || autoFireRes.get() && !mc.player.getActiveEffectsMap().containsKey(MobEffects.FIRE_RESISTANCE)) && !Modules.get().get(ScaffoldHIG.class).hasWorked();
    }

    private void startEating() {
        if (mc.player == null || mc.gameMode == null) return;

        eating = true;
        justStarted = false;

        if (mc.player.isUsingItem()) return;

        mc.options.keyUse.setDown(true);
        mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
    }

    private void stopEating() {
        if (mc.player == null) return;

        mc.options.keyUse.setDown(false);
        mc.player.releaseUsingItem();
        mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, mc.player.blockPosition(), Direction.DOWN));
        eating = false;
    }

    public boolean isEating() {
        return isActive() && eating;
    }

    public enum Item {
        EGap(Items.ENCHANTED_GOLDEN_APPLE),
        Totem(Items.TOTEM_OF_UNDYING);

        final net.minecraft.world.item.Item item;

        Item(net.minecraft.world.item.Item item) {
            this.item = item;
        }
    }
}
