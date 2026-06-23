package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Insulated MV gold cable — buffers EU and forwards it to neighbors with a small loss.
 */
public class GoldCableBlockEntity extends BaseCableBlockEntity {

    public static final double BUFFER_CAPACITY = 128.0D;
    public static final double TRANSFER_LOSS = 0.2D;

    public GoldCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
                BlockEntityRegistry.GOLD_CABLE_BE.get(),
                pos,
                state,
                BUFFER_CAPACITY,
                TRANSFER_LOSS,
                EnergyTier.MV,
                true);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final GoldCableBlockEntity cable) {
        cable.tickServer();
    }
}
