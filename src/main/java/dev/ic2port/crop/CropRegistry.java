package dev.ic2port.crop;

import dev.ic2port.Reference;
import dev.ic2port.api.crops.ICrop;
import dev.ic2port.crop.ClassicCropRegistrar;
import dev.ic2port.crop.builtin.AdamantumCrop;
import dev.ic2port.crop.builtin.AlumenCrop;
import dev.ic2port.crop.builtin.ArgentumCrop;
import dev.ic2port.crop.builtin.AureliaCrop;
import dev.ic2port.crop.builtin.BambooCrop;
import dev.ic2port.crop.builtin.BeetrootCrop;
import dev.ic2port.crop.builtin.BlackthornCrop;
import dev.ic2port.crop.builtin.BrownMushroomCrop;
import dev.ic2port.crop.builtin.CactusCrop;
import dev.ic2port.crop.builtin.CarrotCrop;
import dev.ic2port.crop.builtin.CinnabarCrop;
import dev.ic2port.crop.builtin.CocoaCrop;
import dev.ic2port.crop.builtin.CoffeaCrop;
import dev.ic2port.crop.builtin.CrimsonFungusCrop;
import dev.ic2port.crop.builtin.CupricumCrop;
import dev.ic2port.crop.builtin.DandelionCrop;
import dev.ic2port.crop.builtin.FerroCrop;
import dev.ic2port.crop.builtin.GlowshroomCrop;
import dev.ic2port.crop.builtin.HempCrop;
import dev.ic2port.crop.builtin.HopsCrop;
import dev.ic2port.crop.builtin.InkbergineCrop;
import dev.ic2port.crop.builtin.KelpCrop;
import dev.ic2port.crop.builtin.LumiliaCrop;
import dev.ic2port.crop.builtin.MalachiteCrop;
import dev.ic2port.crop.builtin.MelonCrop;
import dev.ic2port.crop.builtin.NetherWartCrop;
import dev.ic2port.crop.builtin.PoppyCrop;
import dev.ic2port.crop.builtin.PotatoCrop;
import dev.ic2port.crop.builtin.PumpkinCrop;
import dev.ic2port.crop.builtin.RedMushroomCrop;
import dev.ic2port.crop.builtin.RedwheatCrop;
import dev.ic2port.crop.builtin.SeaPickleCrop;
import dev.ic2port.crop.builtin.StannumCrop;
import dev.ic2port.crop.builtin.StickreedCrop;
import dev.ic2port.crop.builtin.SugarcaneCrop;
import dev.ic2port.crop.builtin.TerraWartCrop;
import dev.ic2port.crop.builtin.WeedCrop;
import dev.ic2port.crop.builtin.WheatCrop;
import dev.ic2port.setup.ItemRegistry;
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
    public static ICrop CARROT;
    public static ICrop BEETROOT;
    public static ICrop POTATO;
    public static ICrop MALACHITE;
    public static ICrop ARGENTUM;
    public static ICrop ALUMEN;
    public static ICrop HEMP;
    public static ICrop KELP;
    public static ICrop ADAMANTUM;
    public static ICrop GLOWSHROOM;
    public static ICrop LUMILIA;
    public static ICrop INKBERGINE;
    public static ICrop BLACKTHORN;
    public static ICrop BAMBOO;
    public static ICrop RED_MUSHROOM;
    public static ICrop BROWN_MUSHROOM;
    public static ICrop DANDELION;
    public static ICrop POPPY;
    public static ICrop SEA_PICKLE;
    public static ICrop CRIMSON_FUNGUS;
    public static ICrop REDWHEAT;

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
        CARROT = register(new CarrotCrop());
        BEETROOT = register(new BeetrootCrop());
        POTATO = register(new PotatoCrop());
        MALACHITE = register(new MalachiteCrop());
        ARGENTUM = register(new ArgentumCrop());
        ALUMEN = register(new AlumenCrop());
        HEMP = register(new HempCrop());
        KELP = register(new KelpCrop());
        ADAMANTUM = register(new AdamantumCrop());
        GLOWSHROOM = register(new GlowshroomCrop());
        LUMILIA = register(new LumiliaCrop());
        INKBERGINE = register(new InkbergineCrop());
        BLACKTHORN = register(new BlackthornCrop());
        BAMBOO = register(new BambooCrop());
        RED_MUSHROOM = register(new RedMushroomCrop());
        BROWN_MUSHROOM = register(new BrownMushroomCrop());
        DANDELION = register(new DandelionCrop());
        POPPY = register(new PoppyCrop());
        SEA_PICKLE = register(new SeaPickleCrop());
        CRIMSON_FUNGUS = register(new CrimsonFungusCrop());
        REDWHEAT = register(new RedwheatCrop());

        ClassicCropRegistrar.registerAll();
        ClassicCropRegistrar.registerBaseSeeds();

        registerBaseSeed(Items.WHEAT_SEEDS, new BaseSeedEntry(WHEAT, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.SUGAR_CANE, new BaseSeedEntry(SUGARCANE, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.NETHER_WART, new BaseSeedEntry(NETHER_WART, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.CARROT, new BaseSeedEntry(CARROT, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.BEETROOT_SEEDS, new BaseSeedEntry(BEETROOT, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.POTATO, new BaseSeedEntry(POTATO, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.KELP, new BaseSeedEntry(KELP, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.GLOWSTONE_DUST, new BaseSeedEntry(GLOWSHROOM, 1, 1, 1, 2, 0));
        registerBaseSeed(Items.GLOW_BERRIES, new BaseSeedEntry(LUMILIA, 1, 0, 1, 0, 3));
        registerBaseSeed(Items.INK_SAC, new BaseSeedEntry(INKBERGINE, 2, 2, 0, 0, 1));
        registerBaseSeed(Items.BAMBOO, new BaseSeedEntry(BAMBOO, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.RED_MUSHROOM, new BaseSeedEntry(RED_MUSHROOM, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.BROWN_MUSHROOM, new BaseSeedEntry(BROWN_MUSHROOM, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.DANDELION, new BaseSeedEntry(DANDELION, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.POPPY, new BaseSeedEntry(POPPY, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.SEA_PICKLE, new BaseSeedEntry(SEA_PICKLE, 1, 1, 1, 1, 1));
        registerBaseSeed(Items.CRIMSON_FUNGUS, new BaseSeedEntry(CRIMSON_FUNGUS, 1, 1, 1, 1, 1));
        registerBaseSeed(ItemRegistry.COFFEE_BEAN.get(), new BaseSeedEntry(COFFEA, 1, 1, 1, 1, 1));

        dev.ic2port.setup.AddonApiForgeEvents.fireCropRegisterEvent();
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
