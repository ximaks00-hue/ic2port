package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** LV cable with comparator output proportional to EU flow. */
public class DetectorCableBlockEntity extends BaseCableBlockEntity {

    public static final double BUFFER_CAPACITY = 32.0D;
    public static final double TRANSFER_LOSS = 0.2D;

    private double currentFlow;
    private double lastTickFlow;

    public DetectorCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
                BlockEntityRegistry.DETECTOR_CABLE_BE.get(),
                pos,
                state,
                BUFFER_CAPACITY,
                TRANSFER_LOSS,
                EnergyTier.LV,
                true);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final DetectorCableBlockEntity cable) {
        cable.tickCable();
    }

    @Override
    public void tickCable() {
        lastTickFlow = currentFlow;
        currentFlow = 0.0D;
        super.tickCable();
    }

    @Override
    protected void onEnergyForwarded(final double energyBefore, final double energyAfter) {
        double forwarded = Math.max(0.0D, energyBefore - TRANSFER_LOSS - energyAfter);
        if (forwarded > 0.0D) {
            currentFlow += forwarded;
        }
    }

    @Override
    protected void onEnergyAccepted(final double accepted) {
        currentFlow += accepted;
    }

    public int getRedstoneStrength() {
        if (lastTickFlow < 1.0D) {
            return 0;
        }
        return Math.min(15, (int) Math.ceil(lastTickFlow * 15.0D / BUFFER_CAPACITY));
    }
}
