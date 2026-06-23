package dev.ic2port.api.recipes;

import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the Forge event bus during common setup so addons can register {@link IMachineRecipe} entries.
 */
public class MachineRecipeRegistryEvent extends Event {

  private final Registry registrar;

  public MachineRecipeRegistryEvent(final Registry registrar) {
    this.registrar = registrar;
  }

  public Registry getRegistry() {
    return registrar;
  }

  /**
   * Callback surface passed to addon listeners.
   */
  public interface Registry {

    void register(IMachineRecipe recipe);
  }
}
