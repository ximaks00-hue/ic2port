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
 * Recipe for the Alloy Smelter: two ingredients → one alloy output (e.g., bronze, steel).
 */
public class AlloySmelterRecipe implements Recipe<Container>, IMachineRecipe {

    private final ResourceLocation id;
    private final Ingredient inputA;
    private final Ingredient inputB;
    private final ItemStack output;
    private final double energyCost;
    private final int processingTime;

    public AlloySmelterRecipe(final ResourceLocation id, final Ingredient inputA, final Ingredient inputB,
                               final ItemStack output, final double energyCost, final int processingTime) {
        this.id = id;
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;
        this.energyCost = energyCost;
        this.processingTime = processingTime;
    }

    @Override public double getEnergyCost() { return energyCost; }
    @Override public int getProcessingTime() { return processingTime; }

    @Override
    public boolean matches(final Container container, final Level level) {
        ItemStack a = container.getItem(0);
        ItemStack b = container.getItem(1);
        return !a.isEmpty() && !b.isEmpty() && inputA.test(a) && inputB.test(b);
    }

    @Override
    public ItemStack assemble(final Container container, final RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override public boolean canCraftInDimensions(final int w, final int h) { return true; }
    @Override public ItemStack getResultItem(final RegistryAccess registryAccess) { return output.copy(); }
    @Override public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.ALLOY_SMELTER_SERIALIZER.get();
    }

    @Override public RecipeType<?> getType() { return RecipeTypeRegistry.ALLOY_SMELTER.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, inputA, inputB);
    }

    public Ingredient getInputA() { return inputA; }
    public Ingredient getInputB() { return inputB; }
    public ItemStack getOutput() { return output; }
}
