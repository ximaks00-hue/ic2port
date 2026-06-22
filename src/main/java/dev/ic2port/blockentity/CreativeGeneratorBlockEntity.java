package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creative EU source for testing — emits 32 EU/t (LV) to adjacent acceptors each server tick.
 */
public class CreativeGeneratorBlockEntity extends BlockEntity implements IEnergyEmitter {

    public static final double GENERATION_PER_TICK = 32.0D;
    private static final int TIER = EnergyTier.LV;

    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    public CreativeGeneratorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CREATIVE_GENERATOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final CreativeGeneratorBlockEntity generator) {
        generator.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        double remaining = GENERATION_PER_TICK;
        for (Direction direction : Direction.values()) {
            if (remaining <= 0.0D) {
                break;
            }
            remaining = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, direction, remaining, TIER);
        }
    }

    @Override
    public double getCapacity() {
        return 0.0D;
    }

    @Override
    public double getStoredEnergy() {
        return 0.0D;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    public double getOfferedEnergy() {
        return GENERATION_PER_TICK;
    }

    @Override
    public void drawEnergy(final double amount) {
        // Unlimited creative source — nothing to deduct.
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

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
