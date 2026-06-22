package dev.ic2port.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

/**
 * Recipe datagen hook. Smelting/blasting for dusts and crushed ores live in
 * {@code src/main/resources/data/ic2port/recipes/} to avoid duplicate IDs when {@code runData} runs.
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(final PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(final Consumer<FinishedRecipe> consumer) {
        // Intentionally empty — machine and smelting recipes are maintained as data files.
    }
}
