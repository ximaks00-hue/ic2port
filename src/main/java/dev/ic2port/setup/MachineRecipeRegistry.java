package dev.ic2port.setup;

import dev.ic2port.api.recipes.IMachineRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal store for addon {@link IMachineRecipe} entries.
 */
public final class MachineRecipeRegistry implements dev.ic2port.api.recipes.MachineRecipeRegistryEvent.Registry {

  public static final MachineRecipeRegistry INSTANCE = new MachineRecipeRegistry();

  private final Map<ResourceLocation, IMachineRecipe> recipes = new LinkedHashMap<>();

  private MachineRecipeRegistry() {
  }

  @Override
  public void register(final IMachineRecipe recipe) {
    recipes.put(recipe.getId(), recipe);
  }

  public List<IMachineRecipe> getRecipesForMachine(final ResourceLocation machineId) {
    List<IMachineRecipe> result = new ArrayList<>();
    for (IMachineRecipe recipe : recipes.values()) {
      if (machineId.equals(recipe.getMachineId())) {
        result.add(recipe);
      }
    }
    return Collections.unmodifiableList(result);
  }

  public Map<ResourceLocation, IMachineRecipe> getAll() {
    return Collections.unmodifiableMap(recipes);
  }
}
