package dev.ic2port.util;

import org.jetbrains.annotations.Nullable;
import dev.ic2port.item.ElectricFoamSprayerItem;
import dev.ic2port.item.ElectricItem;
import dev.ic2port.item.FluidCellItem;
import dev.ic2port.item.HydrationCellItem;
import dev.ic2port.item.ReBatteryItem;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Recipe detection and processing for IC2-style canner machines.
 */
public final class CannerOperationHelper {

    public static final double BATTERY_TRANSFER_EU = 2000.0D;
    public static final int HYDRATION_REPAIR = 32;

    public enum Operation {
        NONE,
        FOAM_REFILL,
        BATTERY_CHARGE,
        HYDRATION_REFILL,
        FOOD_CAN,
        TIN_CAN_PRESS,
        CELL_FILL,
        CELL_EMPTY
    }

    private CannerOperationHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Operation detect(final ItemStack slot0, final ItemStack slot1, final boolean vacuum) {
        if (canFoamRefill(slot0, slot1)) {
            return Operation.FOAM_REFILL;
        }
        if (canBatteryCharge(slot0, slot1)) {
            return Operation.BATTERY_CHARGE;
        }
        if (canHydrationRefill(slot0, slot1)) {
            return Operation.HYDRATION_REFILL;
        }
        if (FoodCanningHelper.canProcessLayout(FoodCanningHelper.detectLayout(slot0, slot1))) {
            return Operation.FOOD_CAN;
        }
        if (vacuum && canTinCanPress(slot0, slot1)) {
            return Operation.TIN_CAN_PRESS;
        }
        if (canCellFill(slot0, slot1)) {
            return Operation.CELL_FILL;
        }
        if (canCellEmpty(slot0, slot1)) {
            return Operation.CELL_EMPTY;
        }
        return Operation.NONE;
    }

    public static Operation detect(final ItemStack tool, final ItemStack supply) {
        return detect(tool, supply, false);
    }

    public static boolean canProcess(final ItemStack slot0, final ItemStack slot1, final boolean vacuum) {
        return detect(slot0, slot1, vacuum) != Operation.NONE;
    }

    public static void finishProcess(
            final ItemStack slot0,
            final ItemStack slot1,
            final Operation operation) {
        switch (operation) {
            case FOAM_REFILL -> finishFoamRefill(slot0, slot1);
            case BATTERY_CHARGE -> finishBatteryCharge(slot0, slot1);
            case HYDRATION_REFILL -> finishHydrationRefill(slot0, slot1);
            case TIN_CAN_PRESS -> finishTinCanPress(slot0, slot1);
            case CELL_FILL -> finishCellFill(slot0, slot1);
            case CELL_EMPTY -> finishCellEmpty(slot0, slot1);
            case FOOD_CAN -> {
            }
            default -> {
            }
        }
    }

    private static void finishFoamRefill(final ItemStack slot0, final ItemStack slot1) {
        ItemStack[] pair = orientFoamRefill(slot0, slot1);
        if (pair == null) {
            return;
        }
        ItemStack tool = pair[0];
        ItemStack supply = pair[1];
        int foamSpace = ElectricFoamSprayerItem.MAX_FOAM - ElectricFoamSprayerItem.getFoamStored(tool);
        int added = Math.min(ElectricFoamSprayerItem.FOAM_PER_PELLET, foamSpace);
        ElectricFoamSprayerItem.setFoamStored(tool, ElectricFoamSprayerItem.getFoamStored(tool) + added);
        supply.shrink(1);
    }

    private static void finishBatteryCharge(final ItemStack slot0, final ItemStack slot1) {
        ItemStack[] pair = orientBatteryCharge(slot0, slot1);
        if (pair == null) {
            return;
        }
        ItemStack batteryStack = pair[0];
        ItemStack crystalStack = pair[1];
        if (!(batteryStack.getItem() instanceof ReBatteryItem battery)
                || !(crystalStack.getItem() instanceof ElectricItem crystal)) {
            return;
        }
        double batterySpace = battery.getMaxEnergy() - battery.getStoredEnergy(batteryStack);
        double crystalStored = crystal.getStoredEnergy(crystalStack);
        double transfer = Math.min(BATTERY_TRANSFER_EU, Math.min(batterySpace, crystalStored));
        if (transfer <= 0.0D) {
            return;
        }
        battery.charge(batteryStack, transfer);
        crystal.drawEnergy(crystalStack, transfer);
    }

    private static void finishHydrationRefill(final ItemStack slot0, final ItemStack slot1) {
        ItemStack[] pair = orientHydrationRefill(slot0, slot1);
        if (pair == null) {
            return;
        }
        ItemStack cell = pair[0];
        ItemStack water = pair[1];
        int repair = Math.min(HYDRATION_REPAIR, cell.getMaxDamage() - cell.getDamageValue());
        if (repair <= 0) {
            return;
        }
        cell.setDamageValue(Math.max(0, cell.getDamageValue() - repair));
        water.shrink(1);
    }

    private static void finishTinCanPress(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.is(ItemRegistry.TIN_PLATE.get())) {
            slot0.shrink(1);
        }
        if (slot1.is(ItemRegistry.TIN_PLATE.get())) {
            slot1.shrink(1);
        }
    }

    private static boolean canFoamRefill(final ItemStack slot0, final ItemStack slot1) {
        return orientFoamRefill(slot0, slot1) != null;
    }

    private static boolean canBatteryCharge(final ItemStack slot0, final ItemStack slot1) {
        return orientBatteryCharge(slot0, slot1) != null;
    }

    private static boolean canHydrationRefill(final ItemStack slot0, final ItemStack slot1) {
        return orientHydrationRefill(slot0, slot1) != null;
    }

    private static boolean canTinCanPress(final ItemStack slot0, final ItemStack slot1) {
        int plates = 0;
        if (slot0.is(ItemRegistry.TIN_PLATE.get())) {
            plates += slot0.getCount();
        }
        if (slot1.is(ItemRegistry.TIN_PLATE.get())) {
            plates += slot1.getCount();
        }
        return plates >= 2;
    }

    private static ItemStack[] orientFoamRefill(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.getItem() instanceof ElectricFoamSprayerItem
                && slot1.is(ItemRegistry.FOAM_PELLET.get())
                && ElectricFoamSprayerItem.getFoamStored(slot0)
                        < ElectricFoamSprayerItem.MAX_FOAM - ElectricFoamSprayerItem.FOAM_PER_PELLET + 1) {
            return new ItemStack[] {slot0, slot1};
        }
        if (slot1.getItem() instanceof ElectricFoamSprayerItem
                && slot0.is(ItemRegistry.FOAM_PELLET.get())
                && ElectricFoamSprayerItem.getFoamStored(slot1)
                        < ElectricFoamSprayerItem.MAX_FOAM - ElectricFoamSprayerItem.FOAM_PER_PELLET + 1) {
            return new ItemStack[] {slot1, slot0};
        }
        return null;
    }

    private static ItemStack[] orientBatteryCharge(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.getItem() instanceof ReBatteryItem battery0
                && slot1.getItem() instanceof ElectricItem crystal1
                && !slot1.is(ItemRegistry.RE_BATTERY.get())
                && battery0.getStoredEnergy(slot0) < battery0.getMaxEnergy()
                && crystal1.getStoredEnergy(slot1) > 0.0D) {
            return new ItemStack[] {slot0, slot1};
        }
        if (slot1.getItem() instanceof ReBatteryItem battery1
                && slot0.getItem() instanceof ElectricItem crystal0
                && !slot0.is(ItemRegistry.RE_BATTERY.get())
                && battery1.getStoredEnergy(slot1) < battery1.getMaxEnergy()
                && crystal0.getStoredEnergy(slot0) > 0.0D) {
            return new ItemStack[] {slot1, slot0};
        }
        return null;
    }

    private static boolean canCellFill(final ItemStack slot0, final ItemStack slot1) {
        return orientCellFill(slot0, slot1) != null;
    }

    private static boolean canCellEmpty(final ItemStack slot0, final ItemStack slot1) {
        return orientCellEmpty(slot0, slot1) != null;
    }

    private static ItemStack[] orientCellFill(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.is(ItemRegistry.FLUID_CELL.get()) && FluidCellItem.isEmpty(slot0)
                && (slot1.is(Items.WATER_BUCKET) || slot1.is(Items.LAVA_BUCKET))) {
            return new ItemStack[]{slot0, slot1};
        }
        if (slot1.is(ItemRegistry.FLUID_CELL.get()) && FluidCellItem.isEmpty(slot1)
                && (slot0.is(Items.WATER_BUCKET) || slot0.is(Items.LAVA_BUCKET))) {
            return new ItemStack[]{slot1, slot0};
        }
        return null;
    }

    private static ItemStack[] orientCellEmpty(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.is(ItemRegistry.FLUID_CELL.get()) && !FluidCellItem.isEmpty(slot0)
                && slot1.is(Items.BUCKET)) {
            return new ItemStack[]{slot0, slot1};
        }
        if (slot1.is(ItemRegistry.FLUID_CELL.get()) && !FluidCellItem.isEmpty(slot1)
                && slot0.is(Items.BUCKET)) {
            return new ItemStack[]{slot1, slot0};
        }
        return null;
    }

    /** Returns [cell, bucket] or null. */
    @Nullable
    public static ItemStack[] orientCellFillPublic(final ItemStack slot0, final ItemStack slot1) {
        return orientCellFill(slot0, slot1);
    }

    /** Returns [cell, emptyBucket] or null. */
    @Nullable
    public static ItemStack[] orientCellEmptyPublic(final ItemStack slot0, final ItemStack slot1) {
        return orientCellEmpty(slot0, slot1);
    }

    private static void finishCellFill(final ItemStack slot0, final ItemStack slot1) {
        // No-op: handled in canner block entity finishOperation to allow slot writeback.
    }

    private static void finishCellEmpty(final ItemStack slot0, final ItemStack slot1) {
        // No-op: handled in canner block entity finishOperation to allow slot writeback.
    }

    private static ItemStack[] orientHydrationRefill(final ItemStack slot0, final ItemStack slot1) {
        if (slot0.is(ItemRegistry.HYDRATION_CELL.get())
                && slot1.is(Items.WATER_BUCKET)
                && slot0.getDamageValue() > 0) {
            return new ItemStack[] {slot0, slot1};
        }
        if (slot1.is(ItemRegistry.HYDRATION_CELL.get())
                && slot0.is(Items.WATER_BUCKET)
                && slot1.getDamageValue() > 0) {
            return new ItemStack[] {slot1, slot0};
        }
        return null;
    }
}
