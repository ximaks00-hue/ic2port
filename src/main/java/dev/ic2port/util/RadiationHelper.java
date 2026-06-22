package dev.ic2port.util;

import dev.ic2port.Reference;
import dev.ic2port.item.IRadioactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Detects radioactive items and uranium ore exposure.
 */
public final class RadiationHelper {

    public static final TagKey<Block> URANIUM_ORES_TAG =
            TagKey.create(Registries.BLOCK, new ResourceLocation("forge", "ores/uranium"));

    private static final int ORE_SCAN_RADIUS = 3;
    private static final String ORE_EXPOSURE_TAG = Reference.MOD_ID + ":ore_radiation";

    private RadiationHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isRadioactiveStack(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IRadioactive;
    }

    public static boolean hasRadioactiveItems(final Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (isRadioactiveStack(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNearExposedUraniumOre(final Player player) {
        Level level = player.level();
        if (level.isClientSide) {
            return false;
        }

        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-ORE_SCAN_RADIUS, -ORE_SCAN_RADIUS, -ORE_SCAN_RADIUS),
                center.offset(ORE_SCAN_RADIUS, ORE_SCAN_RADIUS, ORE_SCAN_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(URANIUM_ORES_TAG)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param oreScanInterval scan surrounding ore every N ticks; use cached value between scans
     */
    public static boolean isExposedToRadiation(final Player player, final int oreScanInterval) {
        boolean oreExposure = player.getPersistentData().getBoolean(ORE_EXPOSURE_TAG);
        if (player.tickCount % oreScanInterval == 0) {
            oreExposure = isNearExposedUraniumOre(player);
            player.getPersistentData().putBoolean(ORE_EXPOSURE_TAG, oreExposure);
        }
        return hasRadioactiveItems(player) || oreExposure;
    }
}
