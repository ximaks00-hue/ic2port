package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Simple LV copper cable — buffers EU and forwards it to neighbors with a small loss.
 */
public class CopperCableBlockEntity extends BaseCableBlockEntity {

    public static final double BUFFER_CAPACITY = 32.0D;
    public static final double TRANSFER_LOSS = 0.2D;

    public CopperCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
                BlockEntityRegistry.COPPER_CABLE_BE.get(),
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
            final CopperCableBlockEntity cable) {
        cable.tickServer();
    }
}
