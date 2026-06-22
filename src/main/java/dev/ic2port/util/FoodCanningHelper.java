package dev.ic2port.util;

import dev.ic2port.setup.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Tracks partial food nutrition while canning and stores source food on filled cans.
 */
public final class FoodCanningHelper {

    public static final String FOOD_POINTS_TAG = "CannerFoodPoints";
    public static final String STORED_FOOD_ID_TAG = "StoredFoodId";
    public static final String STORED_FOOD_DATA_TAG = "StoredFoodData";

    private FoodCanningHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isFoodInput(final ItemStack stack) {
        if (stack.isEmpty()
                || stack.is(ItemRegistry.TIN_CAN.get())
                || stack.is(ItemRegistry.FILLED_TIN_CAN.get())) {
            return false;
        }
        FoodProperties properties = stack.getItem().getFoodProperties();
        return properties != null && properties.getNutrition() > 0;
    }

    public static int getRemainingPoints(final ItemStack food) {
        if (!isFoodInput(food)) {
            return 0;
        }
        CompoundTag tag = food.getTag();
        if (tag != null && tag.contains(FOOD_POINTS_TAG)) {
            return Math.max(0, tag.getInt(FOOD_POINTS_TAG));
        }
        FoodProperties properties = food.getItem().getFoodProperties();
        return properties == null ? 0 : properties.getNutrition();
    }

    public static void consumeOnePoint(final ItemStack food) {
        int remaining = getRemainingPoints(food) - 1;
        if (remaining <= 0) {
            food.removeTagKey(FOOD_POINTS_TAG);
            food.shrink(1);
            return;
        }
        food.getOrCreateTag().putInt(FOOD_POINTS_TAG, remaining);
    }

    public static ItemStack createFilledCan(final ItemStack food) {
        ItemStack filled = new ItemStack(ItemRegistry.FILLED_TIN_CAN.get());
        CompoundTag tag = filled.getOrCreateTag();
        ResourceLocation foodId = ForgeRegistries.ITEMS.getKey(food.getItem());
        if (foodId != null) {
            tag.putString(STORED_FOOD_ID_TAG, foodId.toString());
        }
        if (food.hasTag()) {
            tag.put(STORED_FOOD_DATA_TAG, food.getTag().copy());
        }
        return filled;
    }

    public static Item getStoredFoodItem(final ItemStack filledCan) {
        CompoundTag tag = filledCan.getTag();
        if (tag == null || !tag.contains(STORED_FOOD_ID_TAG)) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString(STORED_FOOD_ID_TAG)));
    }

    public static ItemStack recreateStoredFood(final ItemStack filledCan) {
        Item foodItem = getStoredFoodItem(filledCan);
        if (foodItem == null) {
            return ItemStack.EMPTY;
        }
        ItemStack food = new ItemStack(foodItem);
        CompoundTag tag = filledCan.getTag();
        if (tag != null && tag.contains(STORED_FOOD_DATA_TAG)) {
            food.setTag(tag.getCompound(STORED_FOOD_DATA_TAG).copy());
        }
        return food;
    }

    public record FoodCanLayout(int foodSlot, int tinSlot, ItemStack food, ItemStack tins) {
    }

    public static FoodCanLayout detectLayout(final ItemStack slot0, final ItemStack slot1) {
        if (isFoodInput(slot0) && isTinSupply(slot1)) {
            return new FoodCanLayout(0, 1, slot0, slot1);
        }
        if (isFoodInput(slot1) && isTinSupply(slot0)) {
            return new FoodCanLayout(1, 0, slot1, slot0);
        }
        return null;
    }

    public static boolean canProcessLayout(final FoodCanLayout layout) {
        if (layout == null || getRemainingPoints(layout.food()) <= 0) {
            return false;
        }
        ItemStack tins = layout.tins();
        if (tins.is(ItemRegistry.TIN_CAN.get())) {
            return tins.getCount() == 1;
        }
        if (tins.is(ItemRegistry.FILLED_TIN_CAN.get())) {
            ItemStack preview = createFilledCan(layout.food());
            return ItemStack.isSameItemSameTags(tins, preview) && tins.getCount() < tins.getMaxStackSize();
        }
        return false;
    }

    private static boolean isTinSupply(final ItemStack stack) {
        return stack.is(ItemRegistry.TIN_CAN.get()) || stack.is(ItemRegistry.FILLED_TIN_CAN.get());
    }
}
