package dev.ic2port.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class CentrifugeRecipeSerializer implements RecipeSerializer<CentrifugeRecipe> {

    @Override
    public CentrifugeRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        int inputCount = GsonHelper.getAsInt(json, "count", 1);
        if (json.get("input").getAsJsonObject().has("count")) {
            inputCount = GsonHelper.getAsInt(json.getAsJsonObject("input"), "count", inputCount);
        }
        JsonArray outputs = GsonHelper.getAsJsonArray(json, "outputs");
        double energyCost = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int processingTime = GsonHelper.getAsInt(json, "time", 200);
        return new CentrifugeRecipe(
                recipeId,
                input,
                inputCount,
                CentrifugeRecipe.readOutputs(outputs),
                energyCost,
                processingTime);
    }

    @Override
    public CentrifugeRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        int inputCount = buffer.readVarInt();
        List<CentrifugeRecipe.OutputStack> outputs = CentrifugeRecipe.readOutputs(buffer);
        double energyCost = buffer.readDouble();
        int processingTime = buffer.readVarInt();
        return new CentrifugeRecipe(recipeId, input, inputCount, outputs, energyCost, processingTime);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final CentrifugeRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        buffer.writeVarInt(recipe.getInputCount());
        CentrifugeRecipe.writeOutputs(buffer, recipe.getOutputs());
        buffer.writeDouble(recipe.getEnergyCost());
        buffer.writeVarInt(recipe.getProcessingTime());
    }
}
