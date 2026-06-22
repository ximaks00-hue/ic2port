package dev.ic2port.compat.jei;

import dev.ic2port.Reference;
import dev.ic2port.blockentity.InductionFurnaceBlockEntity;
import dev.ic2port.recipe.CentrifugeRecipe;
import dev.ic2port.recipe.CompressorRecipe;
import dev.ic2port.recipe.ElectricFurnaceRecipe;
import dev.ic2port.recipe.ExtractorRecipe;
import dev.ic2port.recipe.IMachineRecipe;
import dev.ic2port.recipe.MaceratorRecipe;
import dev.ic2port.recipe.MetalFormerRecipe;
import dev.ic2port.setup.RecipeTypeRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI integration for custom machine recipe types.
 */
@JeiPlugin
public class IC2PortJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Reference.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                MachineRecipeCategory.macerator(registration),
                MachineRecipeCategory.extractor(registration),
                MachineRecipeCategory.compressor(registration),
                MachineRecipeCategory.electricFurnace(registration),
                MachineRecipeCategory.metalFormer(registration),
                MachineRecipeCategory.centrifuge(registration),
                MachineRecipeCategory.inductionFurnace(registration));
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final RecipeManager recipeManager = resolveRecipeManager();
        if (recipeManager == null) {
            return;
        }
        registration.addRecipes(MachineRecipeCategory.MACERATOR_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.MACERATOR.get()));
        registration.addRecipes(MachineRecipeCategory.EXTRACTOR_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.EXTRACTOR.get()));
        registration.addRecipes(MachineRecipeCategory.COMPRESSOR_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.COMPRESSOR.get()));
        registration.addRecipes(MachineRecipeCategory.ELECTRIC_FURNACE_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.ELECTRIC_FURNACE.get()));
        registration.addRecipes(MachineRecipeCategory.METAL_FORMER_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.METAL_FORMER.get()));
        registration.addRecipes(MachineRecipeCategory.CENTRIFUGE_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.THERMAL_CENTRIFUGE.get()));
        registration.addRecipes(MachineRecipeCategory.INDUCTION_FURNACE_TYPE,
                recipeManager.getAllRecipesFor(RecipeTypeRegistry.ELECTRIC_FURNACE.get()));
    }

    private static RecipeManager resolveRecipeManager() {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientPacketListener connection = minecraft.getConnection();
        if (connection != null) {
            return connection.getRecipeManager();
        }
        if (minecraft.level != null) {
            return minecraft.level.getRecipeManager();
        }
        return null;
    }

    static List<ItemStack> getRecipeOutputs(final Recipe<?> recipe) {
        final List<ItemStack> outputs = new ArrayList<>();
        if (recipe instanceof MaceratorRecipe macerator) {
            outputs.add(macerator.getOutput());
        } else if (recipe instanceof ExtractorRecipe extractor) {
            outputs.add(extractor.getOutput());
        } else if (recipe instanceof CompressorRecipe compressor) {
            outputs.add(compressor.getOutput());
        } else if (recipe instanceof ElectricFurnaceRecipe furnace) {
            outputs.add(furnace.getOutput());
        } else if (recipe instanceof MetalFormerRecipe former) {
            outputs.add(former.getOutput());
        } else if (recipe instanceof CentrifugeRecipe centrifuge) {
            for (final CentrifugeRecipe.OutputStack output : centrifuge.getOutputs()) {
                outputs.add(output.copy());
            }
        }
        return outputs;
    }

    static Component formatMachineTooltip(final IMachineRecipe recipe) {
        return Component.translatable(
                "jei.ic2port.machine.stats",
                (int) recipe.getEnergyCost(),
                recipe.getProcessingTime());
    }

    static Component formatInductionFurnaceTooltip(final IMachineRecipe recipe) {
        final int effectiveTicks = Math.max(1, recipe.getProcessingTime() / InductionFurnaceBlockEntity.SPEED_DIVISOR);
        return Component.translatable(
                "jei.ic2port.induction_furnace.stats",
                (int) recipe.getEnergyCost(),
                effectiveTicks,
                recipe.getProcessingTime());
    }

    static ItemStack blockStack(final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block) {
        return new ItemStack(block.get());
    }
}
