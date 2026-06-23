package dev.ic2port.blockentity;

import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Voids any fluid inserted from connected sides.
 */
public class VoidPipeBlockEntity extends BlockEntity implements IFluidHandler {

    private final LazyOptional<IFluidHandler> handlerOptional = LazyOptional.of(() -> this);

    public VoidPipeBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.VOID_PIPE_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final VoidPipeBlockEntity pipe) {
        // passive sink — fill accepts and discards
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(final int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(final int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(final int tank, final @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        return resource.getAmount();
    }

    @Override
    public @NotNull FluidStack drain(final FluidStack resource, final FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(final int maxDrain, final FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return handlerOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerOptional.invalidate();
    }
}
