package dev.ic2port.util;

import dev.ic2port.item.IUpgradeItem;
import dev.ic2port.item.IElectricItem;
import dev.ic2port.item.ToolboxItem;
import dev.ic2port.item.WrenchItem;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Validates items allowed inside a {@link ToolboxItem}.
 */
public final class ToolboxFilters {

    private ToolboxFilters() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isAllowed(final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ToolboxItem) {
            return false;
        }

        Item item = stack.getItem();
        if (item instanceof WrenchItem || item instanceof IUpgradeItem) {
            return true;
        }
        if (item instanceof IElectricItem && !(item instanceof ArmorItem)) {
            return true;
        }
        if (item instanceof BlockItem blockItem && isCableBlock(blockItem.getBlock())) {
            return true;
        }
        return false;
    }

    private static boolean isCableBlock(final Block block) {
        return block == BlockRegistry.COPPER_CABLE.get()
                || block == BlockRegistry.GOLD_CABLE.get()
                || block == BlockRegistry.HV_CABLE.get()
                || block == BlockRegistry.GLASS_FIBER_CABLE.get();
    }
}
