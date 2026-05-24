package me.redcarlos.higtools.system;

import me.redcarlos.higtools.HIGTools;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HIGSystem extends System<HIGSystem> {
    public final Settings settings = new Settings();

    private final SettingGroup sgPrefix = settings.createGroup("Prefix");

    // Prefix

    public final Setting<String> prefix = sgPrefix.add(new StringSetting.Builder()
        .name("prefix")
        .description("What prefix to use for HIG modules.")
        .defaultValue("HIG Tools")
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<SettingColor> prefixColor = sgPrefix.add(new ColorSetting.Builder()
        .name("prefix-color")
        .description("Color display for the prefix.")
        .defaultValue(new SettingColor(145, 61, 226, 255))
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<Format> prefixFormat = sgPrefix.add(new EnumSetting.Builder<Format>()
        .name("prefix-format")
        .description("What type of minecraft formatting should be applied to the prefix.")
        .defaultValue(Format.Normal)
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<Boolean> formatBrackets = sgPrefix.add(new BoolSetting.Builder()
        .name("format-brackets")
        .description("Whether the formatting should apply to the brackets as well.")
        .visible(() -> prefixFormat.get() != Format.Normal)
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .defaultValue(true)
        .build()
    );

    public final Setting<String> leftBracket = sgPrefix.add(new StringSetting.Builder()
        .name("left-bracket")
        .description("What to be displayed as left bracket for the prefix.")
        .defaultValue("[")
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<String> rightBracket = sgPrefix.add(new StringSetting.Builder()
        .name("right-bracket")
        .description("What to be displayed as right bracket for the prefix.")
        .defaultValue("]")
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<SettingColor> leftColor = sgPrefix.add(new ColorSetting.Builder()
        .name("left-color")
        .description("Color display for the left bracket.")
        .defaultValue(new SettingColor(150, 150, 150, 255))
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public final Setting<SettingColor> rightColor = sgPrefix.add(new ColorSetting.Builder()
        .name("right-color")
        .description("Color display for the right bracket.")
        .defaultValue(new SettingColor(150, 150, 150, 255))
        .onChanged(p -> ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix))
        .build()
    );

    public HIGSystem() {
        super("hig-tools");
        ChatUtils.registerCustomPrefix("me.redcarlos.higtools", this::getPrefix);
    }

    public static HIGSystem get() {
        return Systems.get(HIGSystem.class);
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.putString("version", HIGTools.VERSION);
        tag.put("settings", settings.toTag());

        return tag;
    }

    @Override
    public HIGSystem fromTag(CompoundTag tag) {
        if (tag.contains("settings")) tag.getCompound("settings");
        return this;
    }

    // Utils

    public Component getPrefix() {
        MutableComponent logo = Component.literal(prefix.get()).withStyle(prefixFormat.get().formatting);
        MutableComponent left = Component.literal(leftBracket.get());
        MutableComponent right = Component.literal(rightBracket.get());

        if (formatBrackets.get()) {
            left = left.withStyle(prefixFormat.get().formatting);
            right = right.withStyle(prefixFormat.get().formatting);
        }

        logo = logo.withColor(prefixColor.get().getPacked());
        left = left.withColor(leftColor.get().getPacked());
        right = right.withColor(rightColor.get().getPacked());

        return Component.empty().append(left).append(logo).append(right).append(" ");
    }

    public enum Format {
        Normal(ChatFormatting.RESET),
        Heavy(ChatFormatting.BOLD),
        Italic(ChatFormatting.ITALIC),
        Underline(ChatFormatting.UNDERLINE),
        Crossed(ChatFormatting.STRIKETHROUGH),
        Cursed(ChatFormatting.OBFUSCATED);

        final ChatFormatting formatting;

        Format(ChatFormatting formatting) {
            this.formatting = formatting;
        }
    }
}
