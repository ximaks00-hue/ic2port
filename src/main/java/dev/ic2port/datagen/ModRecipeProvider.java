package dev.ic2port.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

/**
 * Generates recipe JSON files under {@code data/<modid>/recipes/}.
 * <p>
 * Stub — recipes will be added when machines and items are implemented.
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(final PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(final Consumer<FinishedRecipe> consumer) {
        // Intentionally empty — machine recipes (macerator, compressor, etc.) will be added later.
    }
}
