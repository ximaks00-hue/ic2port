package dev.ic2port.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public class OreWasherRecipeSerializer implements RecipeSerializer<OreWasherRecipe> {

    @Override
    public OreWasherRecipe fromJson(final ResourceLocation id, final JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
        double energy = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int time = GsonHelper.getAsInt(json, "time", 300);
        return new OreWasherRecipe(id, input, output, energy, time);
    }

    @Override
    public OreWasherRecipe fromNetwork(final ResourceLocation id, final FriendlyByteBuf buf) {
        Ingredient input = Ingredient.fromNetwork(buf);
        ItemStack output = buf.readItem();
        double energy = buf.readDouble();
        int time = buf.readVarInt();
        return new OreWasherRecipe(id, input, output, energy, time);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buf, final OreWasherRecipe recipe) {
        recipe.getInput().toNetwork(buf);
        buf.writeItem(recipe.getOutput());
        buf.writeDouble(recipe.getEnergyCost());
        buf.writeVarInt(recipe.getProcessingTime());
    }
}
