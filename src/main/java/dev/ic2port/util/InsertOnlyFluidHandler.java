package dev.ic2port.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * External fluid capability that accepts fill but rejects drain (internal tanks filled by machine logic).
 */
public final class InsertOnlyFluidHandler implements IFluidHandler {

    private final IFluidHandler delegate;

    public InsertOnlyFluidHandler(final IFluidHandler delegate) {
        this.delegate = delegate;
    }

    /** Drains the wrapped tank when dismantling or exploding the block. */
    public IFluidHandler getDelegate() {
        return delegate;
    }

    @Override
    public int getTanks() {
        return delegate.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(final int tank) {
        return delegate.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(final int tank) {
        return delegate.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(final int tank, final @NotNull FluidStack stack) {
        return delegate.isFluidValid(tank, stack);
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        return delegate.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(final FluidStack resource, final FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(final int maxDrain, final FluidAction action) {
        return FluidStack.EMPTY;
    }
}
