package dev.ic2port.item;

import dev.ic2port.menu.FoodStorageModuleMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stores filled tin cans on armor for use with the auto feeder module (IC2 food storage module).
 */
public class FoodStorageModuleItem extends StorageArmorModuleItem {

    public static final int SLOT_COUNT = 4;

    private static final String INVENTORY_TAG = "FoodStorageInventory";

    public FoodStorageModuleItem(final Properties properties) {
        super(properties);
    }

    @Override
    protected String getInventoryTag() {
        return INVENTORY_TAG;
    }

    @Override
    protected int getSlotCount() {
        return SLOT_COUNT;
    }

    @Override
    protected Component getGuiTitle() {
        return Component.translatable("item.ic2port.food_storage_module");
    }

    @Override
    protected AbstractContainerMenu openModuleMenu(
            final int containerId,
            final int moduleSlot,
            final Inventory inventory) {
        return new FoodStorageModuleMenu(containerId, inventory, moduleSlot);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.food_storage_module.hint"));
    }
}
