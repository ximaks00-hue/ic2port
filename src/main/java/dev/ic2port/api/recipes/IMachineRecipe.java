package dev.ic2port.api.recipes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Addon-facing contract for custom machine processing recipes.
 * <p>
 * Recipes are registered during {@link MachineRecipeRegistryEvent} on the Forge event bus.
 */
public interface IMachineRecipe {

  /**
   * @return unique recipe id (typically {@code namespace:path})
   */
  ResourceLocation getId();

  /**
   * @return machine block id this recipe targets (e.g. {@code ic2port:macerator})
   */
  ResourceLocation getMachineId();

  /**
   * @return EU cost per operation
   */
  int getEnergyCost();

  /**
   * @return processing duration in ticks
   */
  int getDurationTicks();

  /**
   * @return whether the given input can start this recipe
   */
  boolean matches(ItemStack input);

  /**
   * @return primary output for the matched input, or empty if no match
   */
  ItemStack getResult(ItemStack input);

  /**
   * Optional secondary output (e.g. recycler by-products).
   */
  default @Nullable ItemStack getSecondaryResult(final ItemStack input) {
    return null;
  }
}
