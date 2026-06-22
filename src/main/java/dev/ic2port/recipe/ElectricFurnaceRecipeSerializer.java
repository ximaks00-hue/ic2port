package dev.ic2port.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ElectricFurnaceRecipeSerializer implements RecipeSerializer<ElectricFurnaceRecipe> {

    @Override
    public ElectricFurnaceRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
        double energyCost = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int processingTime = GsonHelper.getAsInt(json, "time", 200);
        return new ElectricFurnaceRecipe(recipeId, input, output, energyCost, processingTime);
    }

    @Override
    public ElectricFurnaceRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        ItemStack output = buffer.readItem();
        double energyCost = buffer.readDouble();
        int processingTime = buffer.readVarInt();
        return new ElectricFurnaceRecipe(recipeId, input, output, energyCost, processingTime);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final ElectricFurnaceRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        buffer.writeItem(recipe.getOutput());
        buffer.writeDouble(recipe.getEnergyCost());
        buffer.writeVarInt(recipe.getProcessingTime());
    }
}
