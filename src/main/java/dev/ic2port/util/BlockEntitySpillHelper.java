package dev.ic2port.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Drops item and fluid contents when a block is dismantled or destroyed by overload.
 */
public final class BlockEntitySpillHelper {

    private BlockEntitySpillHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void spillAll(
            final Level level,
            final BlockPos pos,
            final @Nullable BlockEntity blockEntity,
            final @Nullable IItemHandler itemHandler) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (itemHandler != null) {
            spillItems(level, pos, itemHandler);
        }
        if (blockEntity != null) {
            spillFluids(level, pos, blockEntity);
        }
    }

    public static void spillItems(final Level level, final BlockPos pos, final IItemHandler handler) {
        final double x = pos.getX() + 0.5D;
        final double y = pos.getY() + 0.5D;
        final double z = pos.getZ() + 0.5D;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.extractItem(slot, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, x, y, z, stack);
            }
        }
    }

    public static void spillFluids(final Level level, final BlockPos pos, final BlockEntity blockEntity) {
        blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
            FluidStack drained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            dropFluidAsBuckets(level, pos, drained);
        });
    }

    public static void dropFluidAsBuckets(final Level level, final BlockPos pos, final FluidStack fluid) {
        if (fluid.isEmpty()) {
            return;
        }
        Item bucket = fluid.getFluid().getBucket();
        if (bucket == null || bucket == Items.AIR) {
            return;
        }
        int amount = fluid.getAmount();
        final double x = pos.getX() + 0.5D;
        final double y = pos.getY() + 0.5D;
        final double z = pos.getZ() + 0.5D;
        while (amount >= 1000) {
            Containers.dropItemStack(level, x, y, z, new ItemStack(bucket));
            amount -= 1000;
        }
    }
}
