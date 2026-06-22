package dev.ic2port.util;

import dev.ic2port.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Determines whether an item counts as ore input for the blast induction furnace.
 */
public final class OreInputHelper {

    private static final TagKey<Item> FORGE_ORES = ItemTags.create(new ResourceLocation("forge", "ores"));
    private static final TagKey<Item> RAW_MATERIALS = ItemTags.create(new ResourceLocation("forge", "raw_materials"));

    private OreInputHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isOreInput(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(FORGE_ORES) || stack.is(RAW_MATERIALS)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !Reference.MOD_ID.equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.contains("crushed") || path.endsWith("_dust") || path.startsWith("raw_");
    }
}
