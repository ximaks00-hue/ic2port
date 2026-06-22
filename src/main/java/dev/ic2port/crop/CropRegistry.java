package dev.ic2port.crop;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.ICrop;
import dev.ic2port.crop.builtin.AureliaCrop;
import dev.ic2port.crop.builtin.CactusCrop;
import dev.ic2port.crop.builtin.CinnabarCrop;
import dev.ic2port.crop.builtin.CocoaCrop;
import dev.ic2port.crop.builtin.CoffeaCrop;
import dev.ic2port.crop.builtin.CupricumCrop;
import dev.ic2port.crop.builtin.FerroCrop;
import dev.ic2port.crop.builtin.HopsCrop;
import dev.ic2port.crop.builtin.MelonCrop;
import dev.ic2port.crop.builtin.NetherWartCrop;
import dev.ic2port.crop.builtin.PumpkinCrop;
import dev.ic2port.crop.builtin.StannumCrop;
import dev.ic2port.crop.builtin.StickreedCrop;
import dev.ic2port.crop.builtin.SugarcaneCrop;
import dev.ic2port.crop.builtin.TerraWartCrop;
import dev.ic2port.crop.builtin.WeedCrop;
import dev.ic2port.crop.builtin.WheatCrop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory registry for IC2-style crops and plantable base seeds.
 */
public final class CropRegistry {

    private static final Map<ResourceLocation, ICrop> CROPS = new HashMap<>();
    private static final Map<Item, BaseSeedEntry> BASE_SEEDS = new HashMap<>();

    public static ICrop WHEAT;
    public static ICrop STICKREED;
    public static ICrop SUGARCANE;
    public static ICrop HOPS;
    public static ICrop NETHER_WART;
    public static ICrop TERRA_WART;
    public static ICrop WEED;
    public static ICrop FERRU;
    public static ICrop AURELIA;
    public static ICrop STANNUM;
    public static ICrop CUPRICUM;
    public static ICrop CINNABAR;
    public static ICrop COFFEA;
    public static ICrop MELON;
    public static ICrop PUMPKIN;
    public static ICrop CACTUS;
    public static ICrop COCOA;

    private CropRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void bootstrap() {
        CROPS.clear();
        BASE_SEEDS.clear();

        WHEAT = register(new WheatCrop());
        STICKREED = register(new StickreedCrop());
        SUGARCANE = register(new SugarcaneCrop());
        HOPS = register(new HopsCrop());
        NETHER_WART = register(new NetherWartCrop());
        TERRA_WART = register(new TerraWartCrop());
        WEED = register(new WeedCrop());
        FERRU = register(new FerroCrop());
        AURELIA = register(new AureliaCrop());
        STANNUM = register(new StannumCrop());
        CUPRICUM = register(new CupricumCrop());
        CINNABAR = register(new CinnabarCrop());
        COFFEA = register(new CoffeaCrop());
        MELON = register(new MelonCrop());
        PUMPKIN = register(new PumpkinCrop());
        CACTUS = register(new CactusCrop());
        COCOA = register(new CocoaCrop());

        registerBaseSeed(Items.WHEAT_SEEDS, new BaseSeedEntry(WHEAT, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.SUGAR_CANE, new BaseSeedEntry(SUGARCANE, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.NETHER_WART, new BaseSeedEntry(NETHER_WART, 1, 1, 1, 1, 1));
    }

    public static ICrop register(final ICrop crop) {
        CROPS.put(crop.id(), crop);
        return crop;
    }

    public static void registerBaseSeed(final Item item, final BaseSeedEntry entry) {
        BASE_SEEDS.put(item, entry);
    }

    public static ICrop get(final ResourceLocation id) {
        return CROPS.get(id);
    }

    public static Map<ResourceLocation, ICrop> getCrops() {
        return Collections.unmodifiableMap(CROPS);
    }

    public static BaseSeedEntry getBaseSeed(final ItemStack stack) {
        BaseSeedEntry dynamic = BaseSeedEntry.fromStack(stack);
        if (dynamic != null) {
            return dynamic;
        }
        if (stack.isEmpty()) {
            return null;
        }
        BaseSeedEntry entry = BASE_SEEDS.get(stack.getItem());
        if (entry != null && entry.matches(stack)) {
            return entry;
        }
        return null;
    }
}
