package dev.ic2port.util;

import dev.ic2port.setup.ItemRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Weighted random rewards when opening a scrap box (IC2-style loot box).
 */
public final class ScrapBoxDrops {

    private record WeightedDrop(Supplier<Item> item, int weight, IntSupplier count) {
        ItemStack createStack() {
            return new ItemStack(item.get(), count.getAsInt());
        }
    }

    private static final List<WeightedDrop> DROPS = List.of(
            drop(ItemRegistry.RUBBER, 120, () -> 1 + 3),
            drop(Items.COAL, 100, () -> 1 + 4),
            drop(ItemRegistry.IRON_DUST, 80, () -> 1 + 2),
            drop(ItemRegistry.COPPER_DUST, 80, () -> 1 + 2),
            drop(ItemRegistry.TIN_DUST, 80, () -> 1 + 2),
            drop(ItemRegistry.STICKY_RESIN, 60, () -> 1 + 2),
            drop(Items.COBBLESTONE, 50, () -> 4 + 8),
            drop(ItemRegistry.BRONZE_INGOT, 40, () -> 1 + 2),
            drop(ItemRegistry.IRON_PLATE, 30, () -> 1),
            drop(ItemRegistry.COPPER_PLATE, 30, () -> 1),
            drop(ItemRegistry.GOLD_DUST, 25, () -> 1 + 2),
            drop(ItemRegistry.RE_BATTERY, 20, () -> 1),
            drop(ItemRegistry.TIN_INGOT, 15, () -> 1 + 2),
            drop(ItemRegistry.MIXED_METAL_INGOT, 12, () -> 1),
            drop(Items.DIAMOND, 8, () -> 1),
            drop(ItemRegistry.ADVANCED_ALLOY, 6, () -> 1),
            drop(ItemRegistry.ENERGY_CRYSTAL, 4, () -> 1),
            drop(ItemRegistry.IRIDIUM, 2, () -> 1),
            drop(ItemRegistry.UU_MATTER, 1, () -> 1),
            drop(ItemRegistry.BRONZE_PLATE, 20, () -> 1),
            drop(ItemRegistry.LAPOTRON_CRYSTAL, 3, () -> 1),
            drop(ItemRegistry.ELECTRONIC_CIRCUIT, 18, () -> 1),
            drop(ItemRegistry.ADVANCED_CIRCUIT, 8, () -> 1),
            drop(Items.EMERALD, 5, () -> 1),
            drop(Items.REDSTONE, 35, () -> 2 + 6),
            drop(Items.LAPIS_LAZULI, 30, () -> 2 + 5),
            drop(ItemRegistry.SCRAP, 15, () -> 1 + 3));

    private static final int TOTAL_WEIGHT = DROPS.stream().mapToInt(WeightedDrop::weight).sum();

    private ScrapBoxDrops() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack roll(final RandomSource random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        for (WeightedDrop entry : DROPS) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.createStack();
            }
        }
        return DROPS.get(0).createStack();
    }

    private static WeightedDrop drop(
            final Supplier<Item> item,
            final int weight,
            final IntSupplier count) {
        return new WeightedDrop(item, weight, count);
    }

    private static WeightedDrop drop(final Item item, final int weight, final IntSupplier count) {
        return new WeightedDrop(() -> item, weight, count);
    }
}
