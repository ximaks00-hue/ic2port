package dev.ic2port.recipe;

import dev.ic2port.setup.RecipeSerializerRegistry;
import dev.ic2port.setup.RecipeTypeRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Recipe for the Electrolyzer machine: one input → up to two outputs.
 */
public class ElectrolyzerRecipe implements Recipe<Container>, IMachineRecipe {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final ItemStack secondaryOutput;
    private final double energyCost;
    private final int processingTime;

    public ElectrolyzerRecipe(final ResourceLocation id, final Ingredient input,
                               final ItemStack output, final ItemStack secondaryOutput,
                               final double energyCost, final int processingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.secondaryOutput = secondaryOutput;
        this.energyCost = energyCost;
        this.processingTime = processingTime;
    }

    @Override public double getEnergyCost() { return energyCost; }
    @Override public int getProcessingTime() { return processingTime; }

    @Override
    public boolean matches(final Container container, final Level level) {
        return !container.getItem(0).isEmpty() && input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(final Container container, final RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override public boolean canCraftInDimensions(final int w, final int h) { return true; }

    @Override
    public ItemStack getResultItem(final RegistryAccess registryAccess) { return output.copy(); }

    @Override public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.ELECTROLYZER_SERIALIZER.get();
    }

    @Override public RecipeType<?> getType() { return RecipeTypeRegistry.ELECTROLYZER.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public Ingredient getInput() { return input; }
    public ItemStack getOutput() { return output; }
    public ItemStack getSecondaryOutput() { return secondaryOutput; }
}
