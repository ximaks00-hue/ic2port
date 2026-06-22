package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MV pump block — drains fluid sources below it at 100 mB/tick.
 * Stores up to 4000 mB internally; expose fluid capability for pipes/canner.
 */
public class PumpBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 10_000.0D;
    public static final double EU_PER_BUCKET = 200.0D;
    public static final int TIER = EnergyTier.MV;
    public static final int TANK_CAPACITY_MB = 4000;
    private static final int PUMP_INTERVAL_TICKS = 10;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public int fill(final FluidStack resource, final FluidAction action) {
            if (!resource.isEmpty() && !getFluid().isEmpty() && !getFluid().isFluidEqual(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }
    };
    private final LazyOptional<IFluidHandler> tankOptional = LazyOptional.of(() -> tank);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int tickCount;

    public PumpBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.PUMP_BE.get(), pos, state);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final PumpBlockEntity pump) {
        pump.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || storedEnergy < EU_PER_BUCKET) return;

        tickCount++;
        if (tickCount < PUMP_INTERVAL_TICKS) return;
        tickCount = 0;

        for (int depth = 1; depth <= 64; depth++) {
            BlockPos target = worldPosition.below(depth);
            if (!level.isInWorldBounds(target)) break;
            BlockState blockState = level.getBlockState(target);
            if (blockState.getFluidState().isSource()) {
                net.minecraft.world.level.material.Fluid fluid = blockState.getFluidState().getType();
                FluidStack toFill = new FluidStack(fluid, 1000);
                int filled = tank.fill(toFill, IFluidHandler.FluidAction.SIMULATE);
                if (filled <= 0) break;
                tank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                level.setBlock(target, Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_ALL);
                storedEnergy -= EU_PER_BUCKET;
                setChanged();
                break;
            } else if (!blockState.isAir() && !blockState.getFluidState().isSource()) {
                break;
            }
        }
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > getTier()) {
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override
    public double getCapacity() { return ENERGY_CAPACITY; }
    @Override
    public double getStoredEnergy() { return storedEnergy; }
    @Override
    public int getTier() { return TIER; }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> capability,
                                                       final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) return energyOptional.cast();
        if (capability == ForgeCapabilities.FLUID_HANDLER) return tankOptional.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        tankOptional.invalidate();
    }

}
