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
 * Creative HV EU source for overload testing — emits 512 EU/t at Tier 3.
 */
public class HvCreativeGeneratorBlockEntity extends BlockEntity implements IEnergyEmitter {

    public static final double GENERATION_PER_TICK = EnergyTier.HV_MAX_PACKET;
    public static final int TIER = EnergyTier.HV;

    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    public HvCreativeGeneratorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.HV_CREATIVE_GENERATOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final HvCreativeGeneratorBlockEntity generator) {
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
