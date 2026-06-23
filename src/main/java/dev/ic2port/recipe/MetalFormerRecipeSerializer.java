package dev.ic2port.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public class MetalFormerRecipeSerializer implements RecipeSerializer<MetalFormerRecipe> {

    @Override
    public MetalFormerRecipe fromJson(final ResourceLocation recipeId, final JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
        double energyCost = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int processingTime = GsonHelper.getAsInt(json, "time", 100);
        MetalFormerMode mode = json.has("mode")
                ? MetalFormerMode.fromString(GsonHelper.getAsString(json, "mode"))
                : MetalFormerMode.DEFAULT;
        return new MetalFormerRecipe(recipeId, input, output, energyCost, processingTime, mode);
    }

    @Override
    public MetalFormerRecipe fromNetwork(final ResourceLocation recipeId, final FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        ItemStack output = buffer.readItem();
        double energyCost = buffer.readDouble();
        int processingTime = buffer.readVarInt();
        MetalFormerMode mode = MetalFormerMode.fromString(buffer.readUtf());
        return new MetalFormerRecipe(recipeId, input, output, energyCost, processingTime, mode);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buffer, final MetalFormerRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        buffer.writeItem(recipe.getOutput());
        buffer.writeDouble(recipe.getEnergyCost());
        buffer.writeVarInt(recipe.getProcessingTime());
        buffer.writeUtf(recipe.getMode().getSerializedName());
    }
}
