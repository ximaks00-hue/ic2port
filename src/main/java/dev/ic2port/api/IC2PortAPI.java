package dev.ic2port.api;

import dev.ic2port.Reference;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.MachineRecipeRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable entry point for IC2 Port addon integrations.
 * <p>
 * Third-party mods should depend on the {@code api} classifier JAR and interact only with
 * types under {@code dev.ic2port.api}.
 */
public final class IC2PortAPI {

  /** Mod identifier ({@value Reference#MOD_ID}). */
  public static final String MOD_ID = Reference.MOD_ID;

  private IC2PortAPI() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * @return highest EU voltage tier supported by this build
   */
  public static int getMaxEnergyTier() {
    return EnergyTier.EV;
  }

  /**
   * @return mod-scoped resource location
   */
  public static ResourceLocation id(final String path) {
    return new ResourceLocation(MOD_ID, path);
  }

  /**
   * @return read-only view of registered machine recipes (empty until {@link dev.ic2port.api.recipes.MachineRecipeRegistryEvent})
   */
  public static MachineRecipeRegistry machineRecipes() {
    return MachineRecipeRegistry.INSTANCE;
  }

}
