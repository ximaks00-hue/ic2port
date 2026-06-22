package dev.ic2port.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public class AlloySmelterRecipeSerializer implements RecipeSerializer<AlloySmelterRecipe> {

    @Override
    public AlloySmelterRecipe fromJson(final ResourceLocation id, final JsonObject json) {
        Ingredient inputA = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_a"));
        Ingredient inputB = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_b"));
        ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "output"), true);
        double energy = GsonHelper.getAsDouble(json, "energy", 0.0D);
        int time = GsonHelper.getAsInt(json, "time", 400);
        return new AlloySmelterRecipe(id, inputA, inputB, output, energy, time);
    }

    @Override
    public AlloySmelterRecipe fromNetwork(final ResourceLocation id, final FriendlyByteBuf buf) {
        Ingredient inputA = Ingredient.fromNetwork(buf);
        Ingredient inputB = Ingredient.fromNetwork(buf);
        ItemStack output = buf.readItem();
        double energy = buf.readDouble();
        int time = buf.readVarInt();
        return new AlloySmelterRecipe(id, inputA, inputB, output, energy, time);
    }

    @Override
    public void toNetwork(final FriendlyByteBuf buf, final AlloySmelterRecipe recipe) {
        recipe.getInputA().toNetwork(buf);
        recipe.getInputB().toNetwork(buf);
        buf.writeItem(recipe.getOutput());
        buf.writeDouble(recipe.getEnergyCost());
        buf.writeVarInt(recipe.getProcessingTime());
    }
}
