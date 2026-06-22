package dev.ic2port.api.energy;

/**
 * Base contract for any participant in the EU (Energy Unit) network.
 * <p>
 * Voltage tiers follow IC2 conventions:
 * <ul>
 *     <li>1 — LV (Low Voltage, 32 EU/t)</li>
 *     <li>2 — MV (Medium Voltage, 128 EU/t)</li>
 *     <li>3 — HV (High Voltage, 512 EU/t)</li>
 *     <li>4 — EV (Extreme Voltage, 2048 EU/t)</li>
 * </ul>
 */
public interface IEnergyNode {

    /**
     * @return maximum amount of EU this node can store or buffer
     */
    double getCapacity();

    /**
     * @return current amount of EU stored in this node
     */
    double getStoredEnergy();

    /**
     * @return voltage tier of this node (1 = LV, 2 = MV, 3 = HV, 4 = EV)
     */
    int getTier();
}
