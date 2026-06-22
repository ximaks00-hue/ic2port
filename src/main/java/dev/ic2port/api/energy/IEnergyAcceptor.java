package dev.ic2port.api.energy;

import net.minecraft.core.Direction;

/**
 * An EU consumer that can receive energy from adjacent sources or conductors.
 * <p>
 * Implementations include machines, energy storage blocks and transformers (input side).
 */
public interface IEnergyAcceptor extends IEnergyNode {

    /**
     * Attempts to inject EU into this node from the given direction.
     *
     * @param directionFrom side the energy arrives from (relative to this block)
     * @param amount        EU to inject
     * @param tier          voltage tier of the incoming packet
     * @return remainder of {@code amount} that could not be accepted
     */
    double injectEnergy(Direction directionFrom, double amount, int tier);
}
