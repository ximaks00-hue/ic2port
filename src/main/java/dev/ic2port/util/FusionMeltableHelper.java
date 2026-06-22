package dev.ic2port.util;

import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Meltable block bonuses for the thermonuclear reactor.
 */
public final class FusionMeltableHelper {

    public static final TagKey<Item> MELTABLE = TagKey.create(
            Registries.ITEM, new ResourceLocation("ic2port", "fusion_meltable"));

    private FusionMeltableHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int getBonusMb(final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.OBSIDIAN)) {
            return 1_000;
        }
        if (stack.is(BlockRegistry.REINFORCED_STONE.get().asItem())
                || stack.is(BlockRegistry.REINFORCED_BRICKS.get().asItem())
                || stack.is(BlockRegistry.REINFORCED_COBBLESTONE.get().asItem())
                || stack.is(BlockRegistry.REINFORCED_CRACKED_STONE.get().asItem())) {
            return 800;
        }
        if (stack.is(Items.DEEPSLATE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            return 650;
        }
        if (stack.is(Items.STONE) || stack.is(Items.SMOOTH_STONE)) {
            return 550;
        }
        if (stack.is(Items.COBBLESTONE)) {
            return 500;
        }
        if (stack.is(Items.BLACKSTONE) || stack.is(Items.BASALT)) {
            return 450;
        }
        if (stack.is(Items.NETHERRACK)) {
            return 250;
        }
        return 400;
    }

    public static boolean isMeltable(final ItemStack stack) {
        return stack.is(MELTABLE);
    }
}
