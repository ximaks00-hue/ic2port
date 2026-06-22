package dev.ic2port.blockentity;

import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.util.FusionReactorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Exports lava from a nearby fusion reactor through the reinforced shell.
 */
public class FusionReactorValveBlockEntity extends BlockEntity {

    public FusionReactorValveBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.FUSION_REACTOR_VALVE_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final FusionReactorValveBlockEntity valve) {
        valve.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || level.getGameTime() % 20 != 0) {
            return;
        }

        FusionReactorBlockEntity reactor = findReactor();
        if (reactor == null || !reactor.isStructureValid()
                || !reactor.isAutoExportLava()
                || reactor.getLavaTank().getFluidAmount() <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor == null) {
                continue;
            }
            IFluidHandler handler = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
                    .orElse(null);
            if (handler == null) {
                continue;
            }
            int drained = reactor.getLavaTank().drain(500, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained <= 0) {
                continue;
            }
            FluidStack toFill = new FluidStack(Fluids.LAVA, drained);
            int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                reactor.getLavaTank().drain(filled, IFluidHandler.FluidAction.EXECUTE);
                reactor.setChanged();
                reactor.notifyComparatorOutput();
                notifyComparatorOutput();
                return;
            }
        }
    }

    @Nullable
    private FusionReactorBlockEntity findReactor() {
        if (level == null) {
            return null;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos check = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(check).is(BlockRegistry.FUSION_REACTOR.get())
                            && level.getBlockEntity(check) instanceof FusionReactorBlockEntity reactor) {
                        return reactor;
                    }
                }
            }
        }
        return null;
    }

    public int getComparatorOutput() {
        FusionReactorBlockEntity reactor = findReactor();
        if (reactor == null) {
            return 0;
        }
        return reactor.getComparatorOutput();
    }

    public void notifyComparatorOutput() {
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }
}
