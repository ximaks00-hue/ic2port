package dev.ic2port.util;



import dev.ic2port.block.ITubeBlock;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TubeRole;

import net.minecraft.core.Direction;

import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;



/**

 * Applies advanced tube configuration from the tube configurator tool.

 */

public final class TubeConfiguratorHelper {



    public enum ConfigMode {

        EXTRA_EXTRACT("extra_extract"),

        BLOCK_OUTPUT("block_output"),

        REDSTONE_CONTROL("redstone_control"),

        OUTPUT_PRIORITY("output_priority"),

        ONLY_EXISTING("only_existing"),

        PICKUP_RADIUS("pickup_radius"),

        PULSE_EXTRACT("pulse_extract"),

        COMPARATOR_OUTPUT("comparator_output");



        private final String translationKey;



        ConfigMode(final String translationKey) {

            this.translationKey = translationKey;

        }



        public String translationKey() {

            return translationKey;

        }



        public ConfigMode next() {

            ConfigMode[] values = values();

            return values[(ordinal() + 1) % values.length];

        }



        public static ConfigMode fromStack(final ItemStack stack) {

            if (!stack.hasTag() || !stack.getTag().contains("TubeConfigMode")) {

                return EXTRA_EXTRACT;

            }

            int index = stack.getTag().getInt("TubeConfigMode");

            ConfigMode[] values = values();

            if (index < 0 || index >= values.length) {

                return EXTRA_EXTRACT;

            }

            return values[index];

        }



        public static void writeToStack(final ItemStack stack, final ConfigMode mode) {

            stack.getOrCreateTag().putInt("TubeConfigMode", mode.ordinal());

        }

    }



    private TubeConfiguratorHelper() {

        throw new UnsupportedOperationException("Utility class");

    }



    public static boolean apply(

            final ConfigMode mode,

            final BlockState state,

            final BlockEntity blockEntity,

            final Direction clickedFace,

            final Player player) {

        if (!(state.getBlock() instanceof ITubeBlock) || !(blockEntity instanceof TubeBlockEntity tube)) {

            return false;

        }

        return switch (mode) {

            case EXTRA_EXTRACT -> applyExtraExtract(tube, clickedFace, player);

            case BLOCK_OUTPUT -> applyBlockOutput(tube, clickedFace, player);

            case REDSTONE_CONTROL -> applyRedstoneControl(tube, player);

            case OUTPUT_PRIORITY -> applyOutputPriority(tube, clickedFace, player);

            case ONLY_EXISTING -> applyOnlyExisting(tube, player);

            case PICKUP_RADIUS -> applyPickupRadius(tube, player);

            case PULSE_EXTRACT -> applyPulseExtract(tube, player);

            case COMPARATOR_OUTPUT -> applyComparatorOutput(tube, player);

        };

    }



    private static boolean applyExtraExtract(

            final TubeBlockEntity tube,

            final Direction clickedFace,

            final Player player) {

        if (!tube.supportsExtraction()) {

            player.displayClientMessage(Component.translatable("message.ic2port.tube.config_invalid"), true);

            return false;

        }

        tube.setExtraExtractDirection(clickedFace);

        player.displayClientMessage(Component.translatable(

                "message.ic2port.tube.config_extra_extract",

                clickedFace.name()), true);

        return true;

    }



    private static boolean applyBlockOutput(

            final TubeBlockEntity tube,

            final Direction clickedFace,

            final Player player) {

        boolean blocked = tube.toggleBlockedOutput(clickedFace);

        player.displayClientMessage(Component.translatable(

                blocked ? "message.ic2port.tube.config_output_blocked" : "message.ic2port.tube.config_output_unblocked",

                clickedFace.name()), true);

        return true;

    }



    private static boolean applyRedstoneControl(final TubeBlockEntity tube, final Player player) {

        boolean enabled = tube.toggleRedstoneControl();

        player.displayClientMessage(Component.translatable(

                enabled ? "message.ic2port.tube.config_redstone_on" : "message.ic2port.tube.config_redstone_off"), true);

        return true;

    }



    private static boolean applyOutputPriority(

            final TubeBlockEntity tube,

            final Direction clickedFace,

            final Player player) {

        tube.setOutputPriority(clickedFace);

        player.displayClientMessage(Component.translatable(

                "message.ic2port.tube.config_output_priority",

                clickedFace.name()), true);

        return true;

    }



    private static boolean applyOnlyExisting(final TubeBlockEntity tube, final Player player) {

        boolean enabled = tube.toggleOnlyExistingInventories();

        player.displayClientMessage(Component.translatable(

                enabled ? "message.ic2port.tube.config_only_existing_on" : "message.ic2port.tube.config_only_existing_off"), true);

        return true;

    }



    private static boolean applyPickupRadius(final TubeBlockEntity tube, final Player player) {

        if (tube.getRole() != TubeRole.PICKUP) {

            player.displayClientMessage(Component.translatable("message.ic2port.tube.config_invalid"), true);

            return false;

        }

        boolean large = tube.toggleLargePickupRadius();

        player.displayClientMessage(Component.translatable(

                large ? "message.ic2port.tube.config_pickup_large_on" : "message.ic2port.tube.config_pickup_large_off"), true);

        return true;

    }



    private static boolean applyPulseExtract(final TubeBlockEntity tube, final Player player) {

        if (!tube.supportsExtraction()) {

            player.displayClientMessage(Component.translatable("message.ic2port.tube.config_invalid"), true);

            return false;

        }

        boolean enabled = tube.togglePulseExtract();

        player.displayClientMessage(Component.translatable(

                enabled ? "message.ic2port.tube.config_pulse_on" : "message.ic2port.tube.config_pulse_off"), true);

        return true;

    }



    private static boolean applyComparatorOutput(final TubeBlockEntity tube, final Player player) {

        boolean enabled = tube.toggleComparatorFromInventories();

        player.displayClientMessage(Component.translatable(

                enabled ? "message.ic2port.tube.config_comparator_on" : "message.ic2port.tube.config_comparator_off"), true);

        return true;

    }

}

