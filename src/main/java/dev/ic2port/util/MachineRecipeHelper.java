package dev.ic2port.util;



import dev.ic2port.setup.MachineRecipeRegistry;
import dev.ic2port.recipe.CentrifugeRecipe;

import dev.ic2port.recipe.IMachineRecipe;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.world.item.crafting.Recipe;

import net.minecraft.world.item.crafting.RecipeType;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;



import java.util.Optional;

import java.util.function.Function;



/**

 * Shared helpers for machine recipe progress and deterministic recipe selection.

 */

public final class MachineRecipeHelper {



    private MachineRecipeHelper() {

        throw new UnsupportedOperationException("Utility class");

    }



    public static boolean shouldResetProgress(

            @Nullable final ResourceLocation previous,

            @Nullable final ResourceLocation current,

            final int progress) {

        return progress > 0

                && previous != null

                && current != null

                && !previous.equals(current);

    }



    public static int clampProgress(final int progress, final int maxProgress) {

        return Math.min(progress, maxProgress);

    }



    /**

     * Prefer exact-item ingredients over tag recipes; fewer alternatives wins ties.

     * Recipe id breaks remaining ties for stable ordering.

     */

    public static int scoreIngredientSpecificity(final Ingredient ingredient, final ItemStack input) {

        final ItemStack[] stacks = ingredient.getItems();

        if (stacks.length == 0) {

            return 0;

        }



        boolean exactMatch = false;

        for (final ItemStack stack : stacks) {

            if (ItemStack.isSameItemSameTags(stack, input)) {

                exactMatch = true;

                break;

            }

        }



        return (exactMatch ? 10_000 : 1_000) - stacks.length;

    }



    public static <T extends Recipe<Container> & IMachineRecipe> Optional<T> resolveSingleInputRecipe(

            final Level level,

            final RecipeType<T> recipeType,

            final Class<T> recipeClass,

            final ItemStack input,

            @Nullable final ResourceLocation cachedRecipeId,

            final Function<T, Ingredient> ingredientGetter) {

        if (input.isEmpty() || level == null) {

            return Optional.empty();

        }



        if (cachedRecipeId != null) {

            final Optional<T> cached = level.getRecipeManager()

                    .byKey(cachedRecipeId)

                    .filter(recipeClass::isInstance)

                    .map(recipeClass::cast)

                    .filter(recipe -> ingredientGetter.apply(recipe).test(input));

            if (cached.isPresent()) {

                return cached;

            }

        }



        T best = null;

        int bestScore = Integer.MIN_VALUE;

        ResourceLocation bestId = null;



        for (final T recipe : level.getRecipeManager().getAllRecipesFor(recipeType)) {

            final Ingredient ingredient = ingredientGetter.apply(recipe);

            if (!ingredient.test(input)) {

                continue;

            }



            final int score = scoreIngredientSpecificity(ingredient, input);

            final ResourceLocation id = recipe.getId();

            if (best == null

                    || score > bestScore

                    || (score == bestScore && id.compareTo(bestId) < 0)) {

                best = recipe;

                bestScore = score;

                bestId = id;

            }

        }



        return Optional.ofNullable(best);
    }

    @FunctionalInterface
    public interface AddonRecipeFactory<T extends Recipe<Container> & IMachineRecipe> {
        T create(dev.ic2port.api.recipes.IMachineRecipe recipe, ItemStack input);
    }

    public static <T extends Recipe<Container> & IMachineRecipe> Optional<T> resolveSingleInputRecipeWithAddons(
            final Level level,
            final ResourceLocation machineId,
            final RecipeType<T> recipeType,
            final Class<T> recipeClass,
            final ItemStack input,
            @Nullable final ResourceLocation cachedRecipeId,
            final Function<T, Ingredient> ingredientGetter,
            final AddonRecipeFactory<T> addonFactory) {
        Optional<T> datapack = resolveSingleInputRecipe(
                level, recipeType, recipeClass, input, cachedRecipeId, ingredientGetter);

        if (cachedRecipeId != null) {
            dev.ic2port.api.recipes.IMachineRecipe cachedAddon =
                    MachineRecipeRegistry.INSTANCE.getAll().get(cachedRecipeId);
            if (cachedAddon != null
                    && machineId.equals(cachedAddon.getMachineId())
                    && cachedAddon.matches(input)) {
                T synthetic = addonFactory.create(cachedAddon, input);
                if (datapack.isEmpty()) {
                    return Optional.of(synthetic);
                }
                return pickBestRecipe(datapack.get(), synthetic, input, ingredientGetter);
            }
        }

        T bestAddon = null;
        int bestAddonScore = Integer.MIN_VALUE;
        ResourceLocation bestAddonId = null;
        for (dev.ic2port.api.recipes.IMachineRecipe addon
                : MachineRecipeRegistry.INSTANCE.getRecipesForMachine(machineId)) {
            if (!addon.matches(input)) {
                continue;
            }
            T synthetic = addonFactory.create(addon, input);
            int score = scoreIngredientSpecificity(ingredientGetter.apply(synthetic), input);
            ResourceLocation id = synthetic.getId();
            if (bestAddon == null
                    || score > bestAddonScore
                    || (score == bestAddonScore && id.compareTo(bestAddonId) < 0)) {
                bestAddon = synthetic;
                bestAddonScore = score;
                bestAddonId = id;
            }
        }

        if (bestAddon == null) {
            return datapack;
        }
        if (datapack.isEmpty()) {
            return Optional.of(bestAddon);
        }
        return pickBestRecipe(datapack.get(), bestAddon, input, ingredientGetter);
    }

    private static <T extends Recipe<Container> & IMachineRecipe> Optional<T> pickBestRecipe(
            final T datapack,
            final T addon,
            final ItemStack input,
            final Function<T, Ingredient> ingredientGetter) {
        int datapackScore = scoreIngredientSpecificity(ingredientGetter.apply(datapack), input);
        int addonScore = scoreIngredientSpecificity(ingredientGetter.apply(addon), input);
        if (addonScore > datapackScore) {
            return Optional.of(addon);
        }
        if (addonScore == datapackScore && addon.getId().compareTo(datapack.getId()) < 0) {
            return Optional.of(addon);
        }
        return Optional.of(datapack);
    }

    public static <T extends Recipe<Container> & IMachineRecipe> boolean acceptsSingleInputWithAddons(
            final Level level,
            final ResourceLocation machineId,
            final RecipeType<T> recipeType,
            final Class<T> recipeClass,
            final ItemStack input,
            final Function<T, Ingredient> ingredientGetter,
            final AddonRecipeFactory<T> addonFactory) {
        return resolveSingleInputRecipeWithAddons(
                level, machineId, recipeType, recipeClass, input, null, ingredientGetter, addonFactory).isPresent();
    }

    public static <T extends Recipe<Container> & IMachineRecipe> boolean acceptsSingleInput(
            final Level level,
            final RecipeType<T> recipeType,
            final Class<T> recipeClass,
            final ItemStack input,
            final Function<T, Ingredient> ingredientGetter) {
        return resolveSingleInputRecipe(level, recipeType, recipeClass, input, null, ingredientGetter).isPresent();
    }

    /**
     * Centrifuge recipes may overlap on partial stacks — prefer the highest input count.

     */

    public static Optional<CentrifugeRecipe> resolveCentrifugeRecipe(

            final Level level,

            final ItemStack input,

            @Nullable final ResourceLocation cachedRecipeId,

            final RecipeType<CentrifugeRecipe> recipeType) {

        if (input.isEmpty() || level == null) {

            return Optional.empty();

        }



        if (cachedRecipeId != null) {

            final Optional<CentrifugeRecipe> cached = level.getRecipeManager()

                    .byKey(cachedRecipeId)

                    .filter(CentrifugeRecipe.class::isInstance)

                    .map(CentrifugeRecipe.class::cast)

                    .filter(recipe -> recipe.matchesInput(input));

            if (cached.isPresent()) {

                return cached;

            }

        }



        CentrifugeRecipe best = null;

        for (final CentrifugeRecipe recipe : level.getRecipeManager().getAllRecipesFor(recipeType)) {

            if (!recipe.matchesInput(input)) {

                continue;

            }

            if (best == null

                    || recipe.getInputCount() > best.getInputCount()

                    || (recipe.getInputCount() == best.getInputCount()

                    && recipe.getId().compareTo(best.getId()) < 0)) {

                best = recipe;

            }

        }



        return Optional.ofNullable(best);
    }

    public static Optional<CentrifugeRecipe> resolveCentrifugeRecipeWithAddons(
            final Level level,
            final ResourceLocation machineId,
            final ItemStack input,
            @Nullable final ResourceLocation cachedRecipeId,
            final RecipeType<CentrifugeRecipe> recipeType,
            final AddonRecipeFactory<CentrifugeRecipe> addonFactory) {
        Optional<CentrifugeRecipe> datapack = resolveCentrifugeRecipe(level, input, cachedRecipeId, recipeType);

        if (cachedRecipeId != null) {
            dev.ic2port.api.recipes.IMachineRecipe cachedAddon =
                    MachineRecipeRegistry.INSTANCE.getAll().get(cachedRecipeId);
            if (cachedAddon != null
                    && machineId.equals(cachedAddon.getMachineId())
                    && cachedAddon.matches(input)) {
                CentrifugeRecipe synthetic = addonFactory.create(cachedAddon, input);
                if (datapack.isEmpty()) {
                    return Optional.of(synthetic);
                }
                return pickBestCentrifugeRecipe(datapack.get(), synthetic, input);
            }
        }

        CentrifugeRecipe bestAddon = null;
        int bestAddonScore = Integer.MIN_VALUE;
        ResourceLocation bestAddonId = null;
        for (dev.ic2port.api.recipes.IMachineRecipe addon
                : MachineRecipeRegistry.INSTANCE.getRecipesForMachine(machineId)) {
            if (!addon.matches(input)) {
                continue;
            }
            CentrifugeRecipe synthetic = addonFactory.create(addon, input);
            int score = scoreIngredientSpecificity(synthetic.getInput(), input);
            ResourceLocation id = synthetic.getId();
            if (bestAddon == null
                    || score > bestAddonScore
                    || (score == bestAddonScore && id.compareTo(bestAddonId) < 0)) {
                bestAddon = synthetic;
                bestAddonScore = score;
                bestAddonId = id;
            }
        }

        if (bestAddon == null) {
            return datapack;
        }
        if (datapack.isEmpty()) {
            return Optional.of(bestAddon);
        }
        return pickBestCentrifugeRecipe(datapack.get(), bestAddon, input);
    }

    private static Optional<CentrifugeRecipe> pickBestCentrifugeRecipe(
            final CentrifugeRecipe datapack,
            final CentrifugeRecipe addon,
            final ItemStack input) {
        int datapackScore = scoreIngredientSpecificity(datapack.getInput(), input);
        int addonScore = scoreIngredientSpecificity(addon.getInput(), input);
        if (addonScore > datapackScore) {
            return Optional.of(addon);
        }
        if (addonScore == datapackScore && addon.getId().compareTo(datapack.getId()) < 0) {
            return Optional.of(addon);
        }
        return Optional.of(datapack);
    }

    public static boolean acceptsCentrifugeInput(
            final Level level,
            final ItemStack input,
            final RecipeType<CentrifugeRecipe> recipeType) {
        return resolveCentrifugeRecipe(level, input, null, recipeType).isPresent();
    }

    public static boolean acceptsCentrifugeInputWithAddons(
            final Level level,
            final ResourceLocation machineId,
            final ItemStack input,
            final RecipeType<CentrifugeRecipe> recipeType,
            final AddonRecipeFactory<CentrifugeRecipe> addonFactory) {
        return resolveCentrifugeRecipeWithAddons(
                level, machineId, input, null, recipeType, addonFactory).isPresent();
    }

}


