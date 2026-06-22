package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.recipe.AlloySmelterRecipe;
import dev.ic2port.recipe.CentrifugeRecipe;
import dev.ic2port.recipe.CompressorRecipe;
import dev.ic2port.recipe.ElectricFurnaceRecipe;
import dev.ic2port.recipe.ElectrolyzerRecipe;
import dev.ic2port.recipe.ExtractorRecipe;
import dev.ic2port.recipe.MetalFormerRecipe;
import dev.ic2port.recipe.MaceratorRecipe;
import dev.ic2port.recipe.OreWasherRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred registry for custom machine {@link RecipeType} instances.
 */
public final class RecipeTypeRegistry {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Reference.MOD_ID);

    public static final RegistryObject<RecipeType<MaceratorRecipe>> MACERATOR =
            RECIPE_TYPES.register("macerator", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "macerator")));

    public static final RegistryObject<RecipeType<ExtractorRecipe>> EXTRACTOR =
            RECIPE_TYPES.register("extractor", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "extractor")));

    public static final RegistryObject<RecipeType<CompressorRecipe>> COMPRESSOR =
            RECIPE_TYPES.register("compressor", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "compressor")));

    public static final RegistryObject<RecipeType<CentrifugeRecipe>> THERMAL_CENTRIFUGE =
            RECIPE_TYPES.register("thermal_centrifuge", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "thermal_centrifuge")));

    public static final RegistryObject<RecipeType<ElectricFurnaceRecipe>> ELECTRIC_FURNACE =
            RECIPE_TYPES.register("electric_furnace", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "electric_furnace")));

    public static final RegistryObject<RecipeType<MetalFormerRecipe>> METAL_FORMER =
            RECIPE_TYPES.register("metal_former", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "metal_former")));

    public static final RegistryObject<RecipeType<ElectrolyzerRecipe>> ELECTROLYZER =
            RECIPE_TYPES.register("electrolyzer", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "electrolyzer")));

    public static final RegistryObject<RecipeType<OreWasherRecipe>> ORE_WASHER =
            RECIPE_TYPES.register("ore_washer", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "ore_washer")));

    public static final RegistryObject<RecipeType<AlloySmelterRecipe>> ALLOY_SMELTER =
            RECIPE_TYPES.register("alloy_smelter", () -> RecipeType.simple(new ResourceLocation(Reference.MOD_ID, "alloy_smelter")));

    private RecipeTypeRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
}
