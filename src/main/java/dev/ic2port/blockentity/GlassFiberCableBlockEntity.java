package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glass fibre cable — EV-tier conductor that never burns out from overload.
 */
public class GlassFiberCableBlockEntity extends BaseCableBlockEntity {

    public static final double BUFFER_CAPACITY = 2048.0D;
    public static final double TRANSFER_LOSS = 0.2D;

    public GlassFiberCableBlockEntity(final BlockPos pos, final BlockState state) {
        super(
                BlockEntityRegistry.GLASS_FIBER_CABLE_BE.get(),
                pos,
                state,
                BUFFER_CAPACITY,
                TRANSFER_LOSS,
                EnergyTier.EV,
                false);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final GlassFiberCableBlockEntity cable) {
        cable.tickServer();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > getTier()) {
            return amount;
        }
        return super.injectEnergy(directionFrom, amount, tier);
    }
}
