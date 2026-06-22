package dev.ic2port.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public class ElectrolyzerRecipeSerializer implements RecipeSerializer<ElectrolyzerRecipe> {

    @Override
    public ElectrolyzerRecipe fromJson(final ResourceLocation id, final JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
        ItemStack secondary = json.has("secondary")
                ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "secondary"), true)
                : ItemStack.EMPTY;
        double energy = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int time = GsonHelper.getAsInt(json, "time", 400);
        return new ElectrolyzerRecipe(id, input, output, secondary, energy, time);
    }

    @Override
    public ElectrolyzerRecipe fromNetwork(final ResourceLocation id, final FriendlyByteBuf buf) {
        Ingredient input = Ingredient.fromNetwork(buf);
        ItemStack output = buf.readItem();
        ItemStack secondary = buf.readItem();
        double energy = buf.readDouble();
        int time = buf.readVarInt();
        return new ElectrolyzerRecipe(id, input, output, secondary, energy, time);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buf, final ElectrolyzerRecipe recipe) {
        recipe.getInput().toNetwork(buf);
        buf.writeItem(recipe.getOutput());
        buf.writeItem(recipe.getSecondaryOutput());
        buf.writeDouble(recipe.getEnergyCost());
        buf.writeVarInt(recipe.getProcessingTime());
    }
}
