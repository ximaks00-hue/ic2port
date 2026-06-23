package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.fluid.FluidPipeNetwork;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyOverloadHelper;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid-network pump — pulls from adjacent tanks and pushes into connected pipes (LV, costs EU).
 */
public class FluidNetworkPumpBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 2_000.0D;
    public static final double EU_PER_MB = 0.2D;
    public static final int PULL_MB = 500;
    public static final int TIER = EnergyTier.LV;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean destroyedByOverload;

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
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }
        double energyCost = EU_PER_MB * PULL_MB;
        if (storedEnergy < energyCost) {
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
            FluidStack drained = source.drain(PULL_MB, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) {
                continue;
            }
            drained = source.drain(PULL_MB, IFluidHandler.FluidAction.EXECUTE);
            if (drained.isEmpty()) {
                continue;
            }
            int moved = FluidPipeNetwork.distribute(level, worldPosition, drained, drained.getAmount());
            if (moved < drained.getAmount()) {
                source.fill(new FluidStack(drained.getFluid(), drained.getAmount() - moved),
                        IFluidHandler.FluidAction.EXECUTE);
            }
            if (moved > 0) {
                storedEnergy -= EU_PER_MB * moved;
                setChanged();
            }
            return;
        }
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > TIER) {
            destroyedByOverload = true;
            EnergyOverloadHelper.tryExplode(level, worldPosition, this, tier, TIER);
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override
    public double getCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putBoolean("DestroyedByOverload", destroyedByOverload);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = tag.getDouble("StoredEnergy");
        destroyedByOverload = tag.getBoolean("DestroyedByOverload");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return super.getCapability(capability, side);
    }
}
