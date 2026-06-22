package dev.ic2port.util;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.blockentity.NuclearReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;

public final class ReactorGridHelper {

    public static final int DEFAULT_COLUMN_COUNT = 3;
    public static final int MAX_CHAMBERS = 6;

    private ReactorGridHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int countAdjacentChambers(final Level level, final BlockPos reactorPos, final Block chamberBlock) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(reactorPos.relative(direction)).is(chamberBlock)) {
                count++;
            }
        }
        return Math.min(MAX_CHAMBERS, count);
    }

    public static int enabledColumnWidth(final int chamberCount) {
        return Math.min(NuclearReactorBlockEntity.GRID_WIDTH, DEFAULT_COLUMN_COUNT + chamberCount);
    }

    public static int enabledColumnStart(final int chamberCount) {
        int width = enabledColumnWidth(chamberCount);
        return (NuclearReactorBlockEntity.GRID_WIDTH - width) / 2;
    }

    public static boolean isColumnEnabled(final int x, final int chamberCount) {
        int start = enabledColumnStart(chamberCount);
        return x >= start && x < start + enabledColumnWidth(chamberCount);
    }

    public static void forEachNeighbor(
            final IReactor reactor,
            final int x,
            final int y,
            final BiConsumer<Integer, Integer> consumer) {
        if (x > 0 && reactor.isColumnEnabled(x - 1)) {
            consumer.accept(x - 1, y);
        }
        if (x + 1 < NuclearReactorBlockEntity.GRID_WIDTH && reactor.isColumnEnabled(x + 1)) {
            consumer.accept(x + 1, y);
        }
        if (y > 0 && reactor.isColumnEnabled(x)) {
            consumer.accept(x, y - 1);
        }
        if (y + 1 < NuclearReactorBlockEntity.GRID_HEIGHT && reactor.isColumnEnabled(x)) {
            consumer.accept(x, y + 1);
        }
    }

    public static boolean storesHeat(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IReactorHeatStorage;
    }

    public static double getNeighborHeat(final IReactor reactor, final int x, final int y) {
        ItemStack stack = reactor.getStack(x, y);
        if (!storesHeat(stack)) {
            return 0.0D;
        }
        return ReactorComponentHeat.getHeat(stack);
    }

    public static void transferHeat(
            final ItemStack stack,
            final double maxHeat,
            final double amount) {
        ReactorComponentHeat.addHeat(stack, amount, maxHeat);
    }
}
