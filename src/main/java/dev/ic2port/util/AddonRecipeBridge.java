package dev.ic2port.util;

import dev.ic2port.api.recipes.IMachineRecipe;
import dev.ic2port.recipe.CompressorRecipe;
import dev.ic2port.recipe.CentrifugeRecipe;
import dev.ic2port.recipe.ElectricFurnaceRecipe;
import dev.ic2port.recipe.ElectrolyzerRecipe;
import dev.ic2port.recipe.ExtractorRecipe;
import dev.ic2port.recipe.MaceratorRecipe;
import dev.ic2port.recipe.MetalFormerMode;
import dev.ic2port.recipe.MetalFormerRecipe;
import dev.ic2port.recipe.OreWasherRecipe;
import dev.ic2port.recipe.AlloySmelterRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts addon {@link IMachineRecipe} entries into datapack-style recipe objects for machine BEs.
 */
public final class AddonRecipeBridge {

    private AddonRecipeBridge() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MaceratorRecipe toMacerator(final IMachineRecipe addon, final ItemStack input) {
        return new MaceratorRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static CompressorRecipe toCompressor(final IMachineRecipe addon, final ItemStack input) {
        return new CompressorRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static ExtractorRecipe toExtractor(final IMachineRecipe addon, final ItemStack input) {
        return new ExtractorRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static ElectricFurnaceRecipe toElectricFurnace(final IMachineRecipe addon, final ItemStack input) {
        return new ElectricFurnaceRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static ElectrolyzerRecipe toElectrolyzer(final IMachineRecipe addon, final ItemStack input) {
        ItemStack secondary = addon.getSecondaryResult(input);
        return new ElectrolyzerRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                secondary == null ? ItemStack.EMPTY : secondary,
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static OreWasherRecipe toOreWasher(final IMachineRecipe addon, final ItemStack input) {
        return new OreWasherRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static AlloySmelterRecipe toAlloySmelter(final IMachineRecipe addon, final ItemStack input) {
        Ingredient ingredient = Ingredient.of(input.getItem());
        return new AlloySmelterRecipe(
                addon.getId(),
                ingredient,
                ingredient,
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }

    public static MetalFormerRecipe toMetalFormer(
            final IMachineRecipe addon,
            final ItemStack input,
            final MetalFormerMode mode) {
        return new MetalFormerRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                addon.getResult(input),
                addon.getEnergyCost(),
                addon.getDurationTicks(),
                mode);
    }

    public static CentrifugeRecipe toCentrifuge(final IMachineRecipe addon, final ItemStack input) {
        List<CentrifugeRecipe.OutputStack> outputs = new ArrayList<>();
        ItemStack primary = addon.getResult(input);
        if (!primary.isEmpty()) {
            outputs.add(new CentrifugeRecipe.OutputStack(primary));
        }
        ItemStack secondary = addon.getSecondaryResult(input);
        if (secondary != null && !secondary.isEmpty()) {
            outputs.add(new CentrifugeRecipe.OutputStack(secondary));
        }
        return new CentrifugeRecipe(
                addon.getId(),
                Ingredient.of(input.getItem()),
                1,
                outputs,
                addon.getEnergyCost(),
                addon.getDurationTicks());
    }
}
