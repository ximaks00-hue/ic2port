package dev.ic2port.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes only the first {@code processSlotCount} slots of a machine inventory to hoppers and pipes.
 */
public final class ProcessOnlyItemHandler implements IItemHandlerModifiable {

    private final IItemHandlerModifiable delegate;
    private final int processSlotCount;

    public ProcessOnlyItemHandler(final IItemHandlerModifiable delegate, final int processSlotCount) {
        this.delegate = delegate;
        this.processSlotCount = processSlotCount;
    }

    @Override
    public int getSlots() {
        return processSlotCount;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(final int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(final int slot, final @NotNull ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }

    @Override
    public @NotNull ItemStack insertItem(final int slot, final @NotNull ItemStack stack, final boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(final int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(final int slot, final @NotNull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}
