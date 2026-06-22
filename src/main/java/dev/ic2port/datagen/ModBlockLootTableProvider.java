package dev.ic2port.datagen;

import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * Loot tables for all mod blocks (machines drop themselves when mined with a pickaxe).
 */
public class ModBlockLootTableProvider extends BlockLootSubProvider {

    public ModBlockLootTableProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (final RegistryObject<Block> entry : BlockRegistry.BLOCKS.getEntries()) {
            final Block block = entry.get();
            if (block == BlockRegistry.TIN_ORE.get()) {
                dropOther(block, ItemRegistry.RAW_TIN.get());
            } else if (block == BlockRegistry.DEEPSLATE_TIN_ORE.get()) {
                dropOther(block, ItemRegistry.RAW_TIN.get());
            } else if (block == BlockRegistry.URANIUM_ORE.get()) {
                dropOther(block, ItemRegistry.RAW_URANIUM.get());
            } else if (block == BlockRegistry.DEEPSLATE_URANIUM_ORE.get()) {
                dropOther(block, ItemRegistry.RAW_URANIUM.get());
            } else if (block == BlockRegistry.RUBBER_WOOD.get()) {
                addRubberWoodDrops(block);
            } else if (block == BlockRegistry.RUBBER_LEAVES.get()) {
                addRubberLeavesDrops(block);
            } else {
                dropSelf(block);
            }
        }
    }

    private void addRubberWoodDrops(final Block block) {
        final LootTable.Builder builder = LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.25F))
                        .add(LootItem.lootTableItem(ItemRegistry.STICKY_RESIN.get())));
        add(block, builder);
    }

    private void addRubberLeavesDrops(final Block block) {
        final LootTable.Builder builder = createOakLeavesDrops(
                        block,
                        BlockRegistry.RUBBER_SAPLING.get(),
                        0.05F)
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.05F))
                        .add(LootItem.lootTableItem(ItemRegistry.STICKY_RESIN.get()))))
                .withPool(applyExplosionCondition(block, LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.02F))
                        .add(LootItem.lootTableItem(Items.STICK))));
        add(block, builder);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get).toList();
    }
}
