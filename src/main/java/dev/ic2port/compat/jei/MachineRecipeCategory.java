package dev.ic2port.compat.jei;

import dev.ic2port.Reference;
import dev.ic2port.recipe.IMachineRecipe;
import dev.ic2port.setup.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Shared JEI category for single-input machine recipes.
 */
public final class MachineRecipeCategory<T extends Recipe<?> & IMachineRecipe> implements IRecipeCategory<T> {

    public static final RecipeType<dev.ic2port.recipe.MaceratorRecipe> MACERATOR_TYPE =
            RecipeType.create(Reference.MOD_ID, "macerator", dev.ic2port.recipe.MaceratorRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.ExtractorRecipe> EXTRACTOR_TYPE =
            RecipeType.create(Reference.MOD_ID, "extractor", dev.ic2port.recipe.ExtractorRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.CompressorRecipe> COMPRESSOR_TYPE =
            RecipeType.create(Reference.MOD_ID, "compressor", dev.ic2port.recipe.CompressorRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.ElectricFurnaceRecipe> ELECTRIC_FURNACE_TYPE =
            RecipeType.create(Reference.MOD_ID, "electric_furnace", dev.ic2port.recipe.ElectricFurnaceRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.MetalFormerRecipe> METAL_FORMER_TYPE =
            RecipeType.create(Reference.MOD_ID, "metal_former", dev.ic2port.recipe.MetalFormerRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.CentrifugeRecipe> CENTRIFUGE_TYPE =
            RecipeType.create(Reference.MOD_ID, "thermal_centrifuge", dev.ic2port.recipe.CentrifugeRecipe.class);
    public static final RecipeType<dev.ic2port.recipe.ElectricFurnaceRecipe> INDUCTION_FURNACE_TYPE =
            RecipeType.create(Reference.MOD_ID, "induction_furnace", dev.ic2port.recipe.ElectricFurnaceRecipe.class);

    private static final int WIDTH = 96;
    private static final int HEIGHT = 38;

    private final RecipeType<T> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final boolean inductionFurnaceStats;

    private MachineRecipeCategory(
            final RecipeType<T> recipeType,
            final Component title,
            final IDrawable background,
            final IDrawable icon,
            final boolean inductionFurnaceStats) {
        this.recipeType = recipeType;
        this.title = title;
        this.background = background;
        this.icon = icon;
        this.inductionFurnaceStats = inductionFurnaceStats;
    }

    static MachineRecipeCategory<dev.ic2port.recipe.MaceratorRecipe> macerator(final IRecipeCategoryRegistration registration) {
        return create(registration, MACERATOR_TYPE, "macerator", BlockRegistry.MACERATOR);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.ExtractorRecipe> extractor(final IRecipeCategoryRegistration registration) {
        return create(registration, EXTRACTOR_TYPE, "extractor", BlockRegistry.EXTRACTOR);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.CompressorRecipe> compressor(final IRecipeCategoryRegistration registration) {
        return create(registration, COMPRESSOR_TYPE, "compressor", BlockRegistry.COMPRESSOR);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.ElectricFurnaceRecipe> electricFurnace(final IRecipeCategoryRegistration registration) {
        return create(registration, ELECTRIC_FURNACE_TYPE, "electric_furnace", BlockRegistry.ELECTRIC_FURNACE);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.MetalFormerRecipe> metalFormer(final IRecipeCategoryRegistration registration) {
        return create(registration, METAL_FORMER_TYPE, "metal_former", BlockRegistry.METAL_FORMER);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.CentrifugeRecipe> centrifuge(final IRecipeCategoryRegistration registration) {
        return create(registration, CENTRIFUGE_TYPE, "thermal_centrifuge", BlockRegistry.THERMAL_CENTRIFUGE);
    }

    static MachineRecipeCategory<dev.ic2port.recipe.ElectricFurnaceRecipe> inductionFurnace(
            final IRecipeCategoryRegistration registration) {
        return create(registration, INDUCTION_FURNACE_TYPE, "induction_furnace", BlockRegistry.INDUCTION_FURNACE, true);
    }

    private static <T extends Recipe<?> & IMachineRecipe> MachineRecipeCategory<T> create(
            final IRecipeCategoryRegistration registration,
            final RecipeType<T> recipeType,
            final String translationKey,
            final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block) {
        return create(registration, recipeType, translationKey, block, false);
    }

    private static <T extends Recipe<?> & IMachineRecipe> MachineRecipeCategory<T> create(
            final IRecipeCategoryRegistration registration,
            final RecipeType<T> recipeType,
            final String translationKey,
            final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block,
            final boolean inductionFurnaceStats) {
        final IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        final IDrawable background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        final IDrawable icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, IC2PortJeiPlugin.blockStack(block));
        return new MachineRecipeCategory<>(
                recipeType,
                Component.translatable("jei.ic2port.category." + translationKey),
                background,
                icon,
                inductionFurnaceStats);
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(final IRecipeLayoutBuilder builder, final T recipe, final IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 11)
                .addIngredients(recipe.getIngredients().get(0));

        final List<ItemStack> outputs = IC2PortJeiPlugin.getRecipeOutputs(recipe);
        int x = 49;
        for (final ItemStack output : outputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, 11).addItemStack(output);
            x += 18;
        }
    }

    @Override
    public void draw(final T recipe, final IRecipeSlotsView recipeSlotsView, final GuiGraphics guiGraphics, final double mouseX, final double mouseY) {
        final Component stats = inductionFurnaceStats
                ? IC2PortJeiPlugin.formatInductionFurnaceTooltip(recipe)
                : IC2PortJeiPlugin.formatMachineTooltip(recipe);
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                stats,
                2,
                27,
                0xFF808080,
                false);
    }
}
