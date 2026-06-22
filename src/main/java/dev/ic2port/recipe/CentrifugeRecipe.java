package dev.ic2port.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import dev.ic2port.setup.RecipeSerializerRegistry;
import dev.ic2port.setup.RecipeTypeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CentrifugeRecipe implements Recipe<Container>, IMachineRecipe {

    public record OutputStack(ItemStack stack) {
        public ItemStack copy() {
            return stack.copy();
        }
    }

    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final List<OutputStack> outputs;
    private final double energyCost;
    private final int processingTime;

    public CentrifugeRecipe(
            final ResourceLocation id,
            final Ingredient input,
            final int inputCount,
            final List<OutputStack> outputs,
            final double energyCost,
            final int processingTime) {
        this.id = id;
        this.input = input;
        this.inputCount = Math.max(1, inputCount);
        this.outputs = List.copyOf(outputs);
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

    public int getInputCount() {
        return inputCount;
    }

    public List<OutputStack> getOutputs() {
        return outputs;
    }

    public boolean matchesInput(final ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= inputCount && input.test(stack);
    }

    @Override
    public boolean matches(final Container container, final Level level) {
        return matchesInput(container.getItem(0));
    }

    @Override
    public ItemStack assemble(final Container container, final RegistryAccess registryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(final RegistryAccess registryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegistry.CENTRIFUGE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypeRegistry.THERMAL_CENTRIFUGE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    public Ingredient getInput() {
        return input;
    }

    public static List<OutputStack> readOutputs(final JsonArray array) {
        List<OutputStack> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            ItemStack stack = CraftingHelper.getItemStack(array.get(i).getAsJsonObject(), true);
            result.add(new OutputStack(stack));
        }
        return result;
    }

    public static void writeOutputs(final FriendlyByteBuf buffer, final List<OutputStack> outputs) {
        buffer.writeVarInt(outputs.size());
        for (OutputStack output : outputs) {
            buffer.writeItem(output.stack());
        }
    }

    public static List<OutputStack> readOutputs(final FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count <= 0) {
            return Collections.emptyList();
        }
        List<OutputStack> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(new OutputStack(buffer.readItem()));
        }
        return result;
    }
}
