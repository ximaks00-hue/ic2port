package dev.ic2port.blockentity;

import dev.ic2port.fluid.FluidPipeNetwork;
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

public class FluidNetworkPumpBlockEntity extends BlockEntity {

    private static final int PULL_MB = 500;

    public FluidNetworkPumpBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.FLUID_PUMP_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final FluidNetworkPumpBlockEntity pump) {
        pump.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null) {
                continue;
            }
            IFluidHandler source = neighbor.getCapability(
                    ForgeCapabilities.FLUID_HANDLER,
                    direction.getOpposite()).orElse(null);
            if (source == null) {
                continue;
            }
            FluidStack drained = source.drain(PULL_MB, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                continue;
            }
            int moved = FluidPipeNetwork.distribute(level, worldPosition, drained, drained.getAmount());
            if (moved < drained.getAmount()) {
                source.fill(new FluidStack(drained.getFluid(), drained.getAmount() - moved),
                        IFluidHandler.FluidAction.EXECUTE);
            }
            return;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        return super.getCapability(capability, side);
    }
}
