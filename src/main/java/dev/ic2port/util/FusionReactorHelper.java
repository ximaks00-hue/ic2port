package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Validates the 5×5×5 IC2-style thermonuclear reactor shell.
 */
public final class FusionReactorHelper {

    public static final int SIZE = 5;
    public static final int HALF = SIZE / 2;

    public static final TagKey<Block> REINFORCED_STONES = TagKey.create(
            Registries.BLOCK, new ResourceLocation("ic2port", "reinforced_stones"));
    public static final TagKey<Block> REINFORCED_GLASSES = TagKey.create(
            Registries.BLOCK, new ResourceLocation("ic2port", "reinforced_glasses"));

    private FusionReactorHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static BlockPos getMinCorner(final BlockPos reactorCenter) {
        return reactorCenter.offset(-HALF, -HALF, -HALF);
    }

    public static boolean isValidShellBlock(final BlockState state, final int dx, final int dy, final int dz) {
        boolean onEdge = dx == 0 || dx == SIZE - 1 || dz == 0 || dz == SIZE - 1;
        boolean onBottom = dy == 0;
        if (onBottom || onEdge) {
            return state.is(REINFORCED_STONES)
                    || state.is(BlockRegistry.FUSION_REACTOR_VALVE.get());
        }
        return state.is(REINFORCED_GLASSES)
                || state.is(BlockRegistry.FUSION_REACTOR_VALVE.get());
    }

    public static boolean isInteriorAir(final BlockState state, final BlockPos worldPos, final BlockPos reactorCenter) {
        if (worldPos.equals(reactorCenter)) {
            return state.is(BlockRegistry.FUSION_REACTOR.get());
        }
        return state.isAir();
    }

    public static boolean validateStructure(final Level level, final BlockPos reactorCenter) {
        BlockPos min = getMinCorner(reactorCenter);
        for (int dy = 0; dy < SIZE; dy++) {
            for (int dz = 0; dz < SIZE; dz++) {
                for (int dx = 0; dx < SIZE; dx++) {
                    BlockPos pos = min.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    boolean interior = dx > 0 && dx < SIZE - 1 && dy > 0 && dy < SIZE - 1 && dz > 0 && dz < SIZE - 1;
                    if (interior) {
                        if (!isInteriorAir(state, pos, reactorCenter)) {
                            return false;
                        }
                    } else if (!isValidShellBlock(state, dx, dy, dz)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static int countFuelRods(final net.minecraftforge.items.IItemHandler handler, final int from, final int to) {
        int count = 0;
        for (int slot = from; slot <= to; slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
