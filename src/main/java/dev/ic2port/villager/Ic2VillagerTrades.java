package dev.ic2port.villager;

import dev.ic2port.Reference;
import dev.ic2port.crop.CropRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.CropSeedHelper;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Trade listings for IC2 Classic villager professions.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class Ic2VillagerTrades {

    private Ic2VillagerTrades() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void registerTrades(final VillagerTradesEvent event) {
        if (event.getType() == Ic2VillagerProfessions.ELECTRIC.get()) {
            addElectricTrades(event.getTrades());
        } else if (event.getType() == Ic2VillagerProfessions.NUCLEAR.get()) {
            addNuclearTrades(event.getTrades());
        } else if (event.getType() == Ic2VillagerProfessions.CROP.get()) {
            addCropTrades(event.getTrades());
        } else if (event.getType() == Ic2VillagerProfessions.DEMO.get()) {
            addDemoTrades(event.getTrades());
        } else if (event.getType() == Ic2VillagerProfessions.BREWING.get()) {
            addBrewingTrades(event.getTrades());
        } else if (event.getType() == Ic2VillagerProfessions.GREG.get()) {
            addGregTrades(event.getTrades());
        }
    }

    private static void addElectricTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(8), ItemRegistry.ELECTRONIC_CIRCUIT.get(), 1, 12, 2));
        trades.get(1).add(offer(emerald(12), ItemRegistry.COPPER_CABLE.get(), 4, 16, 2));
        trades.get(2).add(offer(emerald(16), ItemRegistry.ADVANCED_CIRCUIT.get(), 1, 8, 5));
        trades.get(3).add(offer(emerald(10), ItemRegistry.TIN_PLATE.get(), 4, 12, 10));
        trades.get(4).add(offer(emerald(20), ItemRegistry.BASIC_MACHINE_CASING.get(), 1, 6, 15));
        trades.get(5).add(offer(emerald(32), ItemRegistry.COMPLEX_CIRCUIT.get(), 1, 4, 20));
    }

    private static void addNuclearTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(10), ItemRegistry.EMPTY_FUEL_ROD.get(), 1, 12, 2));
        trades.get(2).add(offer(emerald(12), ItemRegistry.HEAT_VENT.get(), 1, 12, 5));
        trades.get(2).add(offer(emerald(24), ItemRegistry.FUEL_ROD.get(), 1, 8, 5));
        trades.get(3).add(offer(emerald(16), ItemRegistry.COOLANT_CELL.get(), 1, 12, 10));
        trades.get(4).add(offer(emerald(32), ItemRegistry.URANIUM_INGOT.get(), 2, 8, 15));
        trades.get(5).add(offer(
                emerald(48),
                new ItemStack(ItemRegistry.URANIUM_INGOT.get(), 2),
                ItemRegistry.MOX_FUEL_ROD.get(),
                1,
                4,
                20));
    }

    private static void addCropTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(5), ItemRegistry.HOPS.get(), 2, 16, 2));
        trades.get(2).add(seedOffer(emerald(12), CropRegistry.WHEAT, 2, 2, 2, 12));
        trades.get(3).add(seedOffer(emerald(20), CropRegistry.STICKREED, 4, 3, 3, 8));
        trades.get(4).add(offer(emerald(8), ItemRegistry.TEA_LEAF.get(), 4, 12, 15));
        trades.get(5).add(seedOffer(emerald(32), CropRegistry.HOPS, 6, 4, 4, 6));
    }

    private static void addDemoTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(6), Items.GUNPOWDER, 4, 16, 2));
        trades.get(2).add(offer(emerald(16), Items.TNT, 4, 12, 5));
        trades.get(3).add(offer(emerald(20), ItemRegistry.SCRAP.get(), 4, 12, 10));
        trades.get(4).add(offer(emerald(28), ItemRegistry.SCRAP_BOX.get(), 1, 8, 15));
        trades.get(5).add(offer(
                emerald(24),
                new ItemStack(Items.IRON_INGOT, 8),
                Items.TNT,
                16,
                4,
                20));
    }

    private static void addBrewingTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(6), ItemRegistry.BEER.get(), 1, 16, 2));
        trades.get(2).add(offer(emerald(12), ItemRegistry.RUM.get(), 1, 12, 5));
        trades.get(3).add(offer(emerald(20), ItemRegistry.WHISKY.get(), 1, 8, 10));
        trades.get(4).add(offer(emerald(8), ItemRegistry.HOPS.get(), 8, 12, 15));
        trades.get(5).add(offer(emerald(16), ItemRegistry.TEA.get(), 1, 8, 20));
    }

    private static void addGregTrades(final it.unimi.dsi.fastutil.ints.Int2ObjectMap<List<VillagerTrades.ItemListing>> trades) {
        trades.get(1).add(offer(emerald(1), Items.DIRT, 1, 64, 1));
        trades.get(2).add(offer(emerald(16), ItemRegistry.WET_CONSTRUCTION_FOAM.get(), 4, 12, 5));
        trades.get(3).add(offer(emerald(12), ItemRegistry.CONSTRUCTION_FOAM.get(), 1, 12, 10));
        trades.get(4).add(offer(emerald(48), ItemRegistry.FOAM_PELLET.get(), 1, 8, 15));
        trades.get(5).add(offer(
                emerald(64),
                new ItemStack(Items.DIAMOND, 4),
                ItemRegistry.ELECTRIC_FOAM_SPRAYER.get(),
                1,
                2,
                20));
    }

    private static VillagerTrades.ItemListing offer(
            final ItemStack costA,
            final net.minecraft.world.item.Item result,
            final int resultCount,
            final int maxUses,
            final int xp) {
        return (trader, random) -> new MerchantOffer(
                costA.copy(),
                ItemStack.EMPTY,
                new ItemStack(result, resultCount),
                maxUses,
                xp,
                0.05F);
    }

    private static VillagerTrades.ItemListing offer(
            final ItemStack costA,
            final ItemStack costB,
            final net.minecraft.world.item.Item result,
            final int resultCount,
            final int maxUses,
            final int xp) {
        return (trader, random) -> new MerchantOffer(
                costA.copy(),
                costB.copy(),
                new ItemStack(result, resultCount),
                maxUses,
                xp,
                0.05F);
    }

    private static VillagerTrades.ItemListing seedOffer(
            final ItemStack costA,
            final dev.ic2port.api.crops.ICrop crop,
            final int growth,
            final int gain,
            final int resistance,
            final int maxUses) {
        return (trader, random) -> new MerchantOffer(
                costA.copy(),
                ItemStack.EMPTY,
                CropSeedHelper.createSeed(crop, growth, gain, resistance, 0),
                maxUses,
                5,
                0.05F);
    }

    private static ItemStack emerald(final int count) {
        return new ItemStack(Items.EMERALD, count);
    }
}
