package dev.ic2port.util;

import dev.ic2port.setup.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * Helpers for IC2 Port fluid types.
 */
public final class Ic2Fluids {

    private Ic2Fluids() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Fluid steam() {
        return ModFluids.STEAM.isPresent() ? ModFluids.STEAM.get() : Fluids.WATER;
    }

    public static FluidStack steamStack(final int amountMb) {
        return new FluidStack(steam(), amountMb);
    }

    public static boolean isSteam(final FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Fluid fluid = stack.getFluid();
        return fluid == steam() || fluid == ModFluids.STEAM_FLOWING.get();
    }

    /** Migrates legacy water-as-steam tanks to the dedicated steam fluid. */
    public static FluidStack migrateSteamTank(final FluidStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        if (stack.getFluid() == Fluids.WATER) {
            return steamStack(stack.getAmount());
        }
        return stack;
    }
}
