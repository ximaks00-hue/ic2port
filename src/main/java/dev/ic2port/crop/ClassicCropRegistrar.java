package dev.ic2port.crop;

import dev.ic2port.api.crops.CropProperties;
import dev.ic2port.api.crops.ICrop;
import dev.ic2port.crop.builtin.BlueWheatCrop;
import dev.ic2port.crop.builtin.BoneFlowerCrop;
import dev.ic2port.crop.builtin.RainbowFlowerCrop;
import dev.ic2port.crop.builtin.TeaCrop;
import dev.ic2port.crop.builtin.VanillaDropCrop;
import dev.ic2port.crop.builtin.VanillaDropCrop.LightMode;
import dev.ic2port.crop.builtin.VenomiliaCrop;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Registers remaining IC2 Classic crop species.
 */
public final class ClassicCropRegistrar {

    public static ICrop ALLIUM;
    public static ICrop AZURE_BLUET;
    public static ICrop BLUE_ORCHID;
    public static ICrop BLUE_WHEAT;
    public static ICrop BONE_FLOWER;
    public static ICrop BRAIN_CORAL;
    public static ICrop BUBBLE_CORAL;
    public static ICrop CORNFLOWER;
    public static ICrop FIRE_CORAL;
    public static ICrop FORGET_ME_NOT;
    public static ICrop HORN_CORAL;
    public static ICrop LILY_OF_THE_VALLEY;
    public static ICrop ORANGE_TULIP;
    public static ICrop OXEYE_DAISY;
    public static ICrop PINK_TULIP;
    public static ICrop PURPLE_TULIP;
    public static ICrop RAINBOW_FLOWER;
    public static ICrop RED_TULIP;
    public static ICrop SAPLING_ACACIA;
    public static ICrop SAPLING_BIRCH;
    public static ICrop SAPLING_DARK_OAK;
    public static ICrop SAPLING_JUNGLE;
    public static ICrop SAPLING_OAK;
    public static ICrop SAPLING_RUBBERWOOD;
    public static ICrop SAPLING_SPRUCE;
    public static ICrop SEA_GRASS;
    public static ICrop SWEET_BERRY_BUSH;
    public static ICrop TEA;
    public static ICrop TUBE_CORAL;
    public static ICrop VENOMILIA;
    public static ICrop WARPED_FUNGUS;
    public static ICrop WHITE_TULIP;

    private ClassicCropRegistrar() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void registerAll() {
        ALLIUM = flower("allium", Items.ALLIUM);
        AZURE_BLUET = flower("azure_bluet", Items.AZURE_BLUET);
        BLUE_ORCHID = flower("blue_orchid", Items.BLUE_ORCHID);
        BLUE_WHEAT = CropRegistry.register(new BlueWheatCrop());
        BONE_FLOWER = CropRegistry.register(new BoneFlowerCrop());
        BRAIN_CORAL = coral("brain_coral", Items.BRAIN_CORAL);
        BUBBLE_CORAL = coral("bubble_coral", Items.BUBBLE_CORAL);
        CORNFLOWER = flower("cornflower", Items.CORNFLOWER);
        FIRE_CORAL = coral("fire_coral", Items.FIRE_CORAL);
        FORGET_ME_NOT = flower("forget_me_not", Items.AZURE_BLUET);
        HORN_CORAL = coral("horn_coral", Items.HORN_CORAL);
        LILY_OF_THE_VALLEY = flower("lily_of_the_valley", Items.LILY_OF_THE_VALLEY);
        ORANGE_TULIP = flower("orange_tulip", Items.ORANGE_TULIP);
        OXEYE_DAISY = flower("oxeye_daisy", Items.OXEYE_DAISY);
        PINK_TULIP = flower("pink_tulip", Items.PINK_TULIP);
        PURPLE_TULIP = flower("purple_tulip", Items.ALLIUM);
        RAINBOW_FLOWER = CropRegistry.register(new RainbowFlowerCrop());
        RED_TULIP = flower("red_tulip", Items.RED_TULIP);
        SAPLING_ACACIA = sapling("sapling_acacia", Items.ACACIA_SAPLING);
        SAPLING_BIRCH = sapling("sapling_birch", Items.BIRCH_SAPLING);
        SAPLING_DARK_OAK = sapling("sapling_dark_oak", Items.DARK_OAK_SAPLING);
        SAPLING_JUNGLE = sapling("sapling_jungle", Items.JUNGLE_SAPLING);
        SAPLING_OAK = sapling("sapling_oak", Items.OAK_SAPLING);
        SAPLING_RUBBERWOOD = sapling("sapling_rubberwood", ItemRegistry.RUBBER_SAPLING.get());
        SAPLING_SPRUCE = sapling("sapling_spruce", Items.SPRUCE_SAPLING);
        SEA_GRASS = CropRegistry.register(new VanillaDropCrop(
                "sea_grass",
                new CropProperties(2, 0, 1, 1, 1, 0),
                new String[]{"sea", "grass", "green"},
                Items.SEAGRASS,
                8,
                LightMode.ANY));
        SWEET_BERRY_BUSH = CropRegistry.register(new VanillaDropCrop(
                "sweet_berry_bush",
                new CropProperties(2, 1, 0, 0, 1, 0),
                new String[]{"berry", "food", "bush"},
                Items.SWEET_BERRIES,
                7,
                LightMode.BRIGHT));
        TEA = CropRegistry.register(new TeaCrop());
        TUBE_CORAL = coral("tube_coral", Items.TUBE_CORAL);
        VENOMILIA = CropRegistry.register(new VenomiliaCrop());
        WARPED_FUNGUS = CropRegistry.register(new VanillaDropCrop(
                "warped_fungus",
                new CropProperties(2, 0, 2, 0, 2, 0),
                new String[]{"nether", "fungus", "warped"},
                Items.WARPED_FUNGUS,
                7,
                LightMode.LOW_LIGHT));
        WHITE_TULIP = flower("white_tulip", Items.WHITE_TULIP);
    }

    public static void registerBaseSeeds() {
        registerSeed(Items.ALLIUM, ALLIUM);
        registerSeed(Items.AZURE_BLUET, AZURE_BLUET);
        registerSeed(Items.BLUE_ORCHID, BLUE_ORCHID);
        registerSeed(Items.BRAIN_CORAL, BRAIN_CORAL);
        registerSeed(Items.BUBBLE_CORAL, BUBBLE_CORAL);
        registerSeed(Items.CORNFLOWER, CORNFLOWER);
        registerSeed(Items.FIRE_CORAL, FIRE_CORAL);
        registerSeed(Items.HORN_CORAL, HORN_CORAL);
        registerSeed(Items.LILY_OF_THE_VALLEY, LILY_OF_THE_VALLEY);
        registerSeed(Items.ORANGE_TULIP, ORANGE_TULIP);
        registerSeed(Items.OXEYE_DAISY, OXEYE_DAISY);
        registerSeed(Items.PINK_TULIP, PINK_TULIP);
        registerSeed(Items.RED_TULIP, RED_TULIP);
        registerSeed(Items.ACACIA_SAPLING, SAPLING_ACACIA);
        registerSeed(Items.BIRCH_SAPLING, SAPLING_BIRCH);
        registerSeed(Items.DARK_OAK_SAPLING, SAPLING_DARK_OAK);
        registerSeed(Items.JUNGLE_SAPLING, SAPLING_JUNGLE);
        registerSeed(Items.OAK_SAPLING, SAPLING_OAK);
        registerSeed(ItemRegistry.RUBBER_SAPLING.get(), SAPLING_RUBBERWOOD);
        registerSeed(Items.SPRUCE_SAPLING, SAPLING_SPRUCE);
        registerSeed(Items.SEAGRASS, SEA_GRASS);
        registerSeed(Items.SWEET_BERRIES, SWEET_BERRY_BUSH);
        registerSeed(Items.WARPED_FUNGUS, WARPED_FUNGUS);
        registerSeed(Items.WHITE_TULIP, WHITE_TULIP);
        registerSeed(Items.TUBE_CORAL, TUBE_CORAL);
        registerSeed(ItemRegistry.TEA_LEAF.get(), TEA);
        registerSeed(Items.LAPIS_LAZULI, BLUE_WHEAT);
        registerSeed(Items.BONE_MEAL, BONE_FLOWER);
        registerSeed(Items.SPIDER_EYE, VENOMILIA);
    }

    private static ICrop flower(final String id, final Item drop) {
        return CropRegistry.register(new VanillaDropCrop(
                id,
                new CropProperties(2, 1, 0, 0, 1, 0),
                new String[]{"flower", "color"},
                drop,
                10,
                LightMode.ANY));
    }

    private static ICrop coral(final String id, final Item drop) {
        return CropRegistry.register(new VanillaDropCrop(
                id,
                new CropProperties(2, 0, 1, 1, 1, 0),
                new String[]{"coral", "sea"},
                drop,
                8,
                LightMode.ANY));
    }

    private static ICrop sapling(final String id, final Item drop) {
        return CropRegistry.register(new VanillaDropCrop(
                id,
                new CropProperties(3, 1, 1, 0, 2, 0),
                new String[]{"tree", "sapling", "wood"},
                drop,
                6,
                LightMode.BRIGHT));
    }

    private static void registerSeed(final Item item, final ICrop crop) {
        CropRegistry.registerBaseSeed(item, new BaseSeedEntry(crop, 1, 1, 1, 1, 1));
    }
}
