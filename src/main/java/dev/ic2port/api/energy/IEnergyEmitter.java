package dev.ic2port.api.energy;

/**
 * An EU producer that can push energy into adjacent acceptors or conductors.
 * <p>
 * Implementations include generators, solar panels and transformers (output side).
 */
public interface IEnergyEmitter extends IEnergyNode {

    /**
     * @return EU per operation this emitter is willing to offer right now
     */
    double getOfferedEnergy();

    /**
     * Draws EU from this emitter after a successful transfer to an acceptor.
     *
     * @param amount EU that was transferred out
     */
    void drawEnergy(double amount);
}
