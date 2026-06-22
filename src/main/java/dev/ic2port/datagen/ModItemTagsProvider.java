package dev.ic2port.datagen;

import dev.ic2port.Reference;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(
            final PackOutput output,
            final CompletableFuture<HolderLookup.Provider> lookupProvider,
            final CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable final ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        tag(forgeItem("ingots/tin")).add(ItemRegistry.TIN_INGOT.get());
        tag(forgeItem("raw_materials/tin")).add(ItemRegistry.RAW_TIN.get());
        tag(forgeItem("dusts/iron")).add(ItemRegistry.IRON_DUST.get());
        tag(forgeItem("dusts/gold")).add(ItemRegistry.GOLD_DUST.get());
        tag(forgeItem("dusts/copper")).add(ItemRegistry.COPPER_DUST.get());
        tag(forgeItem("dusts/tin")).add(ItemRegistry.TIN_DUST.get());

        tag(forgeItem("ores/tin"))
                .add(BlockRegistry.TIN_ORE.get().asItem())
                .add(BlockRegistry.DEEPSLATE_TIN_ORE.get().asItem());

        tag(forgeItem("ores/uranium"))
                .add(BlockRegistry.URANIUM_ORE.get().asItem())
                .add(BlockRegistry.DEEPSLATE_URANIUM_ORE.get().asItem());

        tag(forgeItem("raw_materials/uranium")).add(ItemRegistry.RAW_URANIUM.get());
        tag(forgeItem("ingots/uranium")).add(ItemRegistry.URANIUM_INGOT.get());
        tag(forgeItem("plates/uranium")).add(ItemRegistry.URANIUM_PLATE.get());

        tag(forgeItem("crushed_ores/iron")).add(ItemRegistry.CRUSHED_IRON_ORE.get());
        tag(forgeItem("crushed_ores/gold")).add(ItemRegistry.CRUSHED_GOLD_ORE.get());
        tag(forgeItem("crushed_ores/copper")).add(ItemRegistry.CRUSHED_COPPER_ORE.get());
        tag(forgeItem("crushed_ores/tin")).add(ItemRegistry.CRUSHED_TIN_ORE.get());

        tag(forgeItem("ingots/bronze")).add(ItemRegistry.BRONZE_INGOT.get());
        tag(forgeItem("plates/iron")).add(ItemRegistry.IRON_PLATE.get());
        tag(forgeItem("plates/copper")).add(ItemRegistry.COPPER_PLATE.get());
        tag(forgeItem("plates/tin")).add(ItemRegistry.TIN_PLATE.get());
        tag(forgeItem("plates/bronze")).add(ItemRegistry.BRONZE_PLATE.get());
        tag(forgeItem("ingots/advanced_alloy")).add(ItemRegistry.ADVANCED_ALLOY.get());

        tag(ItemTags.SAPLINGS).add(BlockRegistry.RUBBER_SAPLING.get().asItem());
    }

    private static TagKey<Item> forgeItem(final String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
    }
}
