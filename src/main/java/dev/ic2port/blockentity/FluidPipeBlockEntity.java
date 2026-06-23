package dev.ic2port.blockentity;

import dev.ic2port.fluid.FluidPipeNetwork;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlockEntity extends BlockEntity implements IFluidHandler {

    public static final int CAPACITY_MB = 1_000;
    public static final int TRANSFER_MB = 200;

    private final FluidTank tank = new FluidTank(CAPACITY_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final LazyOptional<IFluidHandler> tankOptional = LazyOptional.of(() -> this);

    public FluidPipeBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.FLUID_PIPE_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final FluidPipeBlockEntity pipe) {
        pipe.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || tank.getFluidAmount() <= 0) {
            return;
        }
        FluidStack fluid = tank.getFluid().copy();
        fluid.setAmount(Math.min(TRANSFER_MB, fluid.getAmount()));
        int moved = FluidPipeNetwork.distribute(level, worldPosition, fluid, fluid.getAmount());
        if (moved > 0) {
            tank.drain(moved, FluidAction.EXECUTE);
            setChanged();
        }
    }

    public void onTransferred(final int amount) {
        setChanged();
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(final int tank) {
        return this.tank.getFluid();
    }

    @Override
    public int getTankCapacity(final int tank) {
        return CAPACITY_MB;
    }

    @Override
    public boolean isFluidValid(final int tank, final @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(final FluidStack resource, final FluidAction action) {
        return tank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(final FluidStack resource, final FluidAction action) {
        return tank.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(final int maxDrain, final FluidAction action) {
        return tank.drain(maxDrain, action);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return tankOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankOptional.invalidate();
    }
}
