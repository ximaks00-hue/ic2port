package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.recipe.AlloySmelterRecipeSerializer;
import dev.ic2port.recipe.CentrifugeRecipe;
import dev.ic2port.recipe.CentrifugeRecipeSerializer;
import dev.ic2port.recipe.CompressorRecipeSerializer;
import dev.ic2port.recipe.ElectricFurnaceRecipeSerializer;
import dev.ic2port.recipe.ElectrolyzerRecipeSerializer;
import dev.ic2port.recipe.ExtractorRecipeSerializer;
import dev.ic2port.recipe.MetalFormerRecipeSerializer;
import dev.ic2port.recipe.MaceratorRecipeSerializer;
import dev.ic2port.recipe.OreWasherRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Deferred registry for custom machine {@link RecipeSerializer} instances.
 */
public final class RecipeSerializerRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MOD_ID);

    public static final RegistryObject<MaceratorRecipeSerializer> MACERATOR_SERIALIZER =
            RECIPE_SERIALIZERS.register("macerator", MaceratorRecipeSerializer::new);

    public static final RegistryObject<ExtractorRecipeSerializer> EXTRACTOR_SERIALIZER =
            RECIPE_SERIALIZERS.register("extractor", ExtractorRecipeSerializer::new);

    public static final RegistryObject<CompressorRecipeSerializer> COMPRESSOR_SERIALIZER =
            RECIPE_SERIALIZERS.register("compressor", CompressorRecipeSerializer::new);

    public static final RegistryObject<CentrifugeRecipeSerializer> CENTRIFUGE_SERIALIZER =
            RECIPE_SERIALIZERS.register("thermal_centrifuge", CentrifugeRecipeSerializer::new);

    public static final RegistryObject<ElectricFurnaceRecipeSerializer> ELECTRIC_FURNACE_SERIALIZER =
            RECIPE_SERIALIZERS.register("electric_furnace", ElectricFurnaceRecipeSerializer::new);

    public static final RegistryObject<MetalFormerRecipeSerializer> METAL_FORMER_SERIALIZER =
            RECIPE_SERIALIZERS.register("metal_former", MetalFormerRecipeSerializer::new);

    public static final RegistryObject<ElectrolyzerRecipeSerializer> ELECTROLYZER_SERIALIZER =
            RECIPE_SERIALIZERS.register("electrolyzer", ElectrolyzerRecipeSerializer::new);

    public static final RegistryObject<OreWasherRecipeSerializer> ORE_WASHER_SERIALIZER =
            RECIPE_SERIALIZERS.register("ore_washer", OreWasherRecipeSerializer::new);

    public static final RegistryObject<AlloySmelterRecipeSerializer> ALLOY_SMELTER_SERIALIZER =
            RECIPE_SERIALIZERS.register("alloy_smelter", AlloySmelterRecipeSerializer::new);

    private RecipeSerializerRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(final IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
