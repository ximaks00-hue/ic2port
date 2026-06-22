package dev.ic2port.util;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorFuel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * IC2-style reactor pulse and heat formulas.
 */
public final class ReactorMath {

    public static final double BASE_ENERGY_PER_PULSE = 5.0D;
    public static final double BASE_HEAT_PER_PULSE_UNIT = 4.0D;

    private ReactorMath() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int countAdjacentFuelRods(final IReactor reactor, final int x, final int y) {
        int count = 0;
        count += countFuelRodAt(reactor, x - 1, y);
        count += countFuelRodAt(reactor, x + 1, y);
        count += countFuelRodAt(reactor, x, y - 1);
        count += countFuelRodAt(reactor, x, y + 1);
        return count;
    }

    public static int reflectorBonusForCell(final IReactor reactor, final int x, final int y) {
        int bonus = 0;
        bonus += reflectorAt(reactor, x - 1, y);
        bonus += reflectorAt(reactor, x + 1, y);
        bonus += reflectorAt(reactor, x, y - 1);
        bonus += reflectorAt(reactor, x, y + 1);
        return bonus;
    }

    private static int reflectorAt(final IReactor reactor, final int x, final int y) {
        if (!reactor.isInBounds(x, y)) {
            return 0;
        }
        ItemStack stack = reactor.getStack(x, y);
        if (stack.getItem() instanceof dev.ic2port.item.NeutronReflectorItem reflector) {
            return reflector.getReflectionBonus();
        }
        return 0;
    }

    private static int countFuelRodAt(final IReactor reactor, final int x, final int y) {
        if (!reactor.isInBounds(x, y)) {
            return 0;
        }
        ItemStack stack = reactor.getStack(x, y);
        return isActiveFuelRod(stack) ? ((IReactorFuel) stack.getItem()).getRodCount() : 0;
    }

    public static boolean isActiveFuelRod(final ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof IReactorFuel fuel
                && !fuel.isDepleted(stack);
    }

    public static double moxEnergyMultiplier(final double heatRatio) {
        double r = Mth.clamp(heatRatio, 0.0D, 1.0D);
        return 1.0D + r * r * 1.5D;
    }

    public static double moxHeatMultiplier(final double heatRatio) {
        double r = Mth.clamp(heatRatio, 0.0D, 1.0D);
        return 1.0D + r * 0.75D;
    }

    public static int pulsesForNeighbors(final int neighborFuelRods) {
        return 1 + neighborFuelRods;
    }

    public static double energyForPulses(final int pulses) {
        return BASE_ENERGY_PER_PULSE * pulses;
    }

    public static double heatForPulses(final int pulses) {
        return BASE_HEAT_PER_PULSE_UNIT * pulses * (pulses + 1) / 2.0D;
    }
}
