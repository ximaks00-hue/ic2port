package dev.ic2port.util;

import dev.ic2port.setup.ItemRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * Per-fuel production rates for the thermonuclear reactor (IC2-style).
 */
public final class FusionFuelHelper {

    public static final int PRODUCTION_INTERVAL_TICKS = 20;
    public static final int FUEL_CONSUME_INTERVAL_TICKS = 200;

    private FusionFuelHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int getLavaPerCycle(final ItemStack rod) {
        if (rod.is(ItemRegistry.MOX_FUEL_ROD.get())) {
            return 75;
        }
        if (rod.is(ItemRegistry.FUEL_ROD.get())) {
            return 50;
        }
        return 0;
    }

    public static int countProductionRate(final net.minecraftforge.items.IItemHandler handler, final int from, final int to) {
        int total = 0;
        for (int slot = from; slot <= to; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                total += getLavaPerCycle(stack);
            }
        }
        return total;
    }
}
