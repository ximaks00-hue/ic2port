package dev.ic2port.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

/**
 * Automation-facing inventory for EU storage blocks. Hoppers may insert into either slot but only
 * extract finished items (fully charged from the charge slot, fully drained from the discharge slot).
 */
public final class EnergyStorageAutomationHandler implements IItemHandlerModifiable {

    public static final int SLOT_CHARGE = 0;
    public static final int SLOT_DISCHARGE = 1;

    private final IItemHandlerModifiable delegate;
    private final int storageTier;

    public EnergyStorageAutomationHandler(final IItemHandlerModifiable delegate, final int storageTier) {
        this.delegate = delegate;
        this.storageTier = storageTier;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
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
        if (!canExtractFromSlot(slot)) {
            return ItemStack.EMPTY;
        }
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

    private boolean canExtractFromSlot(final int slot) {
        ItemStack stack = delegate.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (slot == SLOT_CHARGE) {
            return !ItemEnergyHelper.canCharge(stack, storageTier);
        }
        if (slot == SLOT_DISCHARGE) {
            return !ItemEnergyHelper.canDischargeInto(stack, storageTier);
        }
        return false;
    }
}
