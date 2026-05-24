package me.redcarlos.higtools.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public class Center extends Command {
    public Center() {
        super("center", "Centers yourself on a block.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
        builder.executes(context -> {
            PlayerUtils.centerPlayer();
            return SINGLE_SUCCESS;
        });
    }
}
