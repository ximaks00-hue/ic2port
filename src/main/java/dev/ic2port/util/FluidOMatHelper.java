package dev.ic2port.util;

import dev.ic2port.blockentity.PersonalTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid-O-Mat helpers for personal tank linking and fluid withdrawal.
 */
public final class FluidOMatHelper {

    public static final int LINK_RADIUS = 3;

    private FluidOMatHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static PersonalTankBlockEntity findLinkedTank(final Level level, final BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-LINK_RADIUS, -LINK_RADIUS, -LINK_RADIUS),
                origin.offset(LINK_RADIUS, LINK_RADIUS, LINK_RADIUS))) {
            if (pos.equals(origin)) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PersonalTankBlockEntity tank) {
                return tank;
            }
        }
        return null;
    }

    @Nullable
    public static Fluid getStoredFluid(final PersonalTankBlockEntity tank) {
        if (tank.getTank().getFluidAmount() <= 0) {
            return null;
        }
        return tank.getTank().getFluid().getFluid();
    }

    public static boolean drainMillibuckets(final PersonalTankBlockEntity tank, final int amount) {
        if (tank.getTank().getFluidAmount() < amount) {
            return false;
        }
        tank.getTank().drain(amount, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }
}
