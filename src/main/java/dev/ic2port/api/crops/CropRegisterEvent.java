package dev.ic2port.api.crops;

import net.minecraftforge.eventbus.api.Event;

/**
 * Fired after built-in crops are registered so addons can add custom {@link ICrop} definitions.
 */
public class CropRegisterEvent extends Event {

  private final Registry registrar;

  public CropRegisterEvent(final Registry registrar) {
    this.registrar = registrar;
  }

  public Registry getRegistry() {
    return registrar;
  }

  /**
   * Callback surface passed to addon listeners.
   */
  public interface Registry {

    ICrop register(ICrop crop);
  }
}
