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
import org.jetbrains.annotations.Nullable;

public class ExtractorRecipe implements Recipe<Container>, IMachineRecipe {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final double energyCost;
    private final int processingTime;

    public ExtractorRecipe(
            final ResourceLocation id,
            final Ingredient input,
            final ItemStack output,
            final double energyCost,
            final int processingTime) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.energyCost = energyCost;
        this.processingTime = processingTime;
    }

    @Override
    public double getEnergyCost() {
        return energyCost;
    }

    @Override
    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        ItemStack stack = container.getItem(0);
        return !stack.isEmpty() && input.test(stack);
    }

    @Override
    public ItemStack assemble(final Container container, final RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(final RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.EXTRACTOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypeRegistry.EXTRACTOR.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public Ingredient getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }
}
