package dev.ic2port.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ic2port.Reference;
import dev.ic2port.setup.ModConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ProfileCommand {

    private ProfileCommand() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(buildRoot());
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildRoot() {
        return Commands.literal("ic2port")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("profile")
                        .executes(ProfileCommand::showStatus)
                        .then(Commands.literal("reactor")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProfiling(
                                                ctx,
                                                "reactor",
                                                ModConfig.REACTOR_PROFILING_ENABLED))))
                        .then(Commands.literal("cable")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProfiling(
                                                ctx,
                                                "cable",
                                                ModConfig.CABLE_PROFILING_ENABLED))))
                        .then(Commands.literal("tube")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(ctx -> setProfiling(
                                                ctx,
                                                "tube",
                                                ModConfig.TUBE_PROFILING_ENABLED)))));
    }

    private static int showStatus(final CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal(
                "[ic2port] Profiling — reactor: "
                        + enabledLabel(ModConfig.REACTOR_PROFILING_ENABLED.get())
                        + " (≥" + ModConfig.REACTOR_PROFILING_THRESHOLD_MS.get() + " ms), "
                        + "cable: "
                        + enabledLabel(ModConfig.CABLE_PROFILING_ENABLED.get())
                        + " (≥" + ModConfig.CABLE_PROFILING_THRESHOLD_MS.get() + " ms), "
                        + "tube: "
                        + enabledLabel(ModConfig.TUBE_PROFILING_ENABLED.get())
                        + " (≥" + ModConfig.TUBE_PROFILING_THRESHOLD_MS.get() + " ms). "
                        + "Use /ic2port profile <reactor|cable|tube> <true|false>."), false);
        return 1;
    }

    private static int setProfiling(
            final CommandContext<CommandSourceStack> context,
            final String name,
            final ForgeConfigSpec.BooleanValue setting) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        setting.set(enabled);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
                () -> Component.literal("[ic2port] " + name + " profiling " + enabledLabel(enabled) + "."),
                true);
        return 1;
    }

    private static String enabledLabel(final boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
}
