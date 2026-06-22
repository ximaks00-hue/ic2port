package dev.ic2port.util;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.crop.CropRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weed checks and stat penalties for IC2-style crops.
 */
public final class CropHelper {

    private CropHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isWeed(final ICropTile tile) {
        ICrop crop = tile.getCrop();
        if (crop == null) {
            return false;
        }
        return crop == CropRegistry.WEED || tile.getGrowthStat() >= 24;
    }

    public static boolean canSpreadWeed(final ICropTile tile) {
        return isWeed(tile) && tile.getGrowthStage() >= 2;
    }

    public static BlockState getFarmlandSubsoil(final CropSticksBlockEntity tile) {
        Level level = tile.getLevel();
        if (level == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockPos farmland = tile.getBlockPos().below();
        if (!level.getBlockState(farmland).is(Blocks.FARMLAND)) {
            return Blocks.AIR.defaultBlockState();
        }
        return level.getBlockState(farmland.below());
    }

    public static boolean hasSnowSubsoil(final CropSticksBlockEntity tile) {
        BlockState subsoil = getFarmlandSubsoil(tile);
        return subsoil.is(Blocks.SNOW_BLOCK) || subsoil.is(Blocks.POWDER_SNOW);
    }

    public static boolean hasSoulSandSubsoil(final CropSticksBlockEntity tile) {
        return getFarmlandSubsoil(tile).is(Blocks.SOUL_SAND);
    }

    public static boolean hasOreSurface(final CropSticksBlockEntity tile) {
        BlockState subsoil = getFarmlandSubsoil(tile);
        return subsoil.is(net.minecraft.world.level.block.Blocks.STONE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.IRON_ORE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_IRON_ORE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.GOLD_ORE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_GOLD_ORE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.COPPER_ORE)
                || subsoil.is(net.minecraft.world.level.block.Blocks.DEEPSLATE_COPPER_ORE);
    }

    public static float getSubsoilGrowthMultiplier(final CropSticksBlockEntity tile) {
        ICrop crop = tile.getCrop();
        if (crop == null) {
            return 1.0F;
        }
        if (crop == CropRegistry.NETHER_WART && hasSoulSandSubsoil(tile)) {
            return 1.5F;
        }
        if (crop == CropRegistry.TERRA_WART && hasSnowSubsoil(tile)) {
            return 1.5F;
        }
        return 1.0F;
    }
}
