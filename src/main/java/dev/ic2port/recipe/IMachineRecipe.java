package dev.ic2port.recipe;

/**
 * Common contract for machine processing recipes (macerator, compressor, etc.).
 * <p>
 * Concrete recipe classes implement this alongside {@link net.minecraft.world.item.crafting.Recipe}.
 */
public interface IMachineRecipe {

    /**
     * @return EU consumed per processing operation
     */
    double getEnergyCost();

    /**
     * @return processing time in ticks
     */
    int getProcessingTime();
}
