package me.redcarlos.higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.item.Items;

public class Coordinates extends Command {
    public Coordinates() {
        super("coordinates", "Copies your coordinates to the clipboard.", "coords");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(_ -> {
            mc.keyboardHandler.setClipboard("%d, %d, %d".formatted(mc.player.blockPosition().getX(), mc.player.blockPosition().getY(), mc.player.blockPosition().getZ()));
            MeteorToast toast = new MeteorToast.Builder("Coordinates").icon(Items.NETHERITE_PICKAXE).text("Copied to clipboard.").build();
            mc.getToastManager().addToast(toast);
            return SINGLE_SUCCESS;
        });
    }
}
