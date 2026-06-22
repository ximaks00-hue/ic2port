package dev.ic2port.util;

import dev.ic2port.api.reactor.IReactorComponent;
import dev.ic2port.item.DepletedFuelRodItem;
import net.minecraft.world.item.ItemStack;

public final class ReactorItemFilters {

    private ReactorItemFilters() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isAllowedInReactor(final ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        return stack.getItem() instanceof IReactorComponent
                || stack.getItem() instanceof DepletedFuelRodItem;
    }
}
