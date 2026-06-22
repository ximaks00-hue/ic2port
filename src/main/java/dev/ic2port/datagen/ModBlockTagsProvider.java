package dev.ic2port.datagen;

import dev.ic2port.Reference;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(
            final PackOutput output,
            final CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable final ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(BlockRegistry.TIN_ORE.get())
                .add(BlockRegistry.DEEPSLATE_TIN_ORE.get())
                .add(BlockRegistry.URANIUM_ORE.get())
                .add(BlockRegistry.DEEPSLATE_URANIUM_ORE.get())
                .add(BlockRegistry.COPPER_CABLE.get())
                .add(BlockRegistry.GOLD_CABLE.get())
                .add(BlockRegistry.HV_CABLE.get())
                .add(BlockRegistry.GLASS_FIBER_CABLE.get())
                .add(BlockRegistry.MACERATOR.get())
                .add(BlockRegistry.RECYCLER.get())
                .add(BlockRegistry.SOLID_FUEL_GENERATOR.get())
                .add(BlockRegistry.GEOTHERMAL_GENERATOR.get())
                .add(BlockRegistry.SOLAR_PANEL.get())
                .add(BlockRegistry.WIND_MILL.get())
                .add(BlockRegistry.WATER_MILL.get())
                .add(BlockRegistry.BATBOX.get())
                .add(BlockRegistry.MFE.get())
                .add(BlockRegistry.MFSU.get())
                .add(BlockRegistry.LV_TRANSFORMER.get())
                .add(BlockRegistry.MV_TRANSFORMER.get())
                .add(BlockRegistry.EV_TRANSFORMER.get())
                .add(BlockRegistry.EXTRACTOR.get())
                .add(BlockRegistry.COMPRESSOR.get())
                .add(BlockRegistry.ELECTRIC_FURNACE.get())
                .add(BlockRegistry.INDUCTION_FURNACE.get())
                .add(BlockRegistry.METAL_FORMER.get())
                .add(BlockRegistry.CHARGE_PAD.get())
                .add(BlockRegistry.THERMAL_CENTRIFUGE.get())
                .add(BlockRegistry.MASS_FABRICATOR.get())
                .add(BlockRegistry.NUCLEAR_REACTOR.get())
                .add(BlockRegistry.REACTOR_CHAMBER.get())
                .add(BlockRegistry.BASIC_MACHINE_CASING.get())
                .add(BlockRegistry.ADVANCED_MACHINE_CASING.get())
                .add(BlockRegistry.CREATIVE_GENERATOR.get())
                .add(BlockRegistry.HV_CREATIVE_GENERATOR.get());

        tag(BlockTags.LOGS).add(BlockRegistry.RUBBER_WOOD.get());
        tag(BlockTags.LOGS_THAT_BURN).add(BlockRegistry.RUBBER_WOOD.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(BlockRegistry.RUBBER_WOOD.get())
                .add(BlockRegistry.RUBBER_LEAVES.get());

        tag(BlockTags.LEAVES)
                .add(BlockRegistry.RUBBER_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(BlockRegistry.RUBBER_SAPLING.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(BlockRegistry.CONTAMINATED_SOIL.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(BlockRegistry.TIN_ORE.get())
                .add(BlockRegistry.DEEPSLATE_TIN_ORE.get())
                .add(BlockRegistry.URANIUM_ORE.get())
                .add(BlockRegistry.DEEPSLATE_URANIUM_ORE.get())
                .add(BlockRegistry.SOLAR_PANEL.get())
                .add(BlockRegistry.WIND_MILL.get())
                .add(BlockRegistry.WATER_MILL.get())
                .add(BlockRegistry.MACERATOR.get())
                .add(BlockRegistry.RECYCLER.get())
                .add(BlockRegistry.SOLID_FUEL_GENERATOR.get())
                .add(BlockRegistry.EXTRACTOR.get())
                .add(BlockRegistry.COMPRESSOR.get())
                .add(BlockRegistry.ELECTRIC_FURNACE.get())
                .add(BlockRegistry.BATBOX.get())
                .add(BlockRegistry.LV_TRANSFORMER.get())
                .add(BlockRegistry.BASIC_MACHINE_CASING.get())
                .add(BlockRegistry.COPPER_CABLE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(BlockRegistry.GEOTHERMAL_GENERATOR.get())
                .add(BlockRegistry.MFE.get())
                .add(BlockRegistry.MV_TRANSFORMER.get())
                .add(BlockRegistry.INDUCTION_FURNACE.get())
                .add(BlockRegistry.METAL_FORMER.get())
                .add(BlockRegistry.CHARGE_PAD.get())
                .add(BlockRegistry.THERMAL_CENTRIFUGE.get())
                .add(BlockRegistry.GOLD_CABLE.get())
                .add(BlockRegistry.ADVANCED_MACHINE_CASING.get());

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(BlockRegistry.EV_TRANSFORMER.get())
                .add(BlockRegistry.MFSU.get())
                .add(BlockRegistry.MASS_FABRICATOR.get())
                .add(BlockRegistry.NUCLEAR_REACTOR.get())
                .add(BlockRegistry.REACTOR_CHAMBER.get())
                .add(BlockRegistry.HV_CABLE.get())
                .add(BlockRegistry.GLASS_FIBER_CABLE.get())
                .add(BlockRegistry.HV_CREATIVE_GENERATOR.get());

        tag(forgeBlock("ores/tin"))
                .add(BlockRegistry.TIN_ORE.get())
                .add(BlockRegistry.DEEPSLATE_TIN_ORE.get());

        tag(forgeBlock("ores/uranium"))
                .add(BlockRegistry.URANIUM_ORE.get())
                .add(BlockRegistry.DEEPSLATE_URANIUM_ORE.get());

        tag(forgeBlock("ores_in_ground/stone"))
                .add(BlockRegistry.TIN_ORE.get())
                .add(BlockRegistry.URANIUM_ORE.get());

        tag(forgeBlock("ores_in_ground/deepslate"))
                .add(BlockRegistry.DEEPSLATE_TIN_ORE.get())
                .add(BlockRegistry.DEEPSLATE_URANIUM_ORE.get());
    }

    private static TagKey<Block> forgeBlock(final String path) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("forge", path));
    }
}
