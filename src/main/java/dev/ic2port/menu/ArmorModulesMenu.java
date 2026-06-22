package dev.ic2port.menu;

import dev.ic2port.item.ArmorModuleItem;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.ArmorModuleHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class ArmorModulesMenu extends AbstractContainerMenu {

    private static final int MODULE_START_X = 62;
    private static final int MODULE_Y = 20;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Inventory playerInventory;
    private final int chestSlot;
    private final ItemStackHandler moduleHandler;

    public ArmorModulesMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readVarInt());
    }

    public ArmorModulesMenu(final int containerId, final Inventory playerInventory, final int chestSlot) {
        super(dev.ic2port.setup.MenuTypeRegistry.ARMOR_MODULES_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.chestSlot = chestSlot;
        ItemStack chestplate = ArmorModuleHelper.getManagedChestplate(playerInventory, chestSlot);
        if (!ArmorModuleHelper.acceptsModules(chestplate)) {
            throw new IllegalStateException("Chest slot does not contain modular chestplate");
        }

        int maxSlots = ArmorModuleHelper.getMaxModuleSlots(chestplate);
        this.moduleHandler = new ItemStackHandler(maxSlots) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack stack) {
                return stack.getItem() instanceof ArmorModuleItem;
            }

            @Override
            protected void onContentsChanged(final int slot) {
                List<ItemStack> modules = new ArrayList<>();
                for (int index = 0; index < getSlots(); index++) {
                    modules.add(getStackInSlot(index));
                }
                ItemStack armor = ArmorModuleHelper.getManagedChestplate(playerInventory, chestSlot);
                ArmorModuleHelper.setModules(armor, modules);
                ArmorModuleHelper.setManagedChestplate(playerInventory, chestSlot, armor);
            }
        };

        List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
        for (int slot = 0; slot < maxSlots; slot++) {
            moduleHandler.setStackInSlot(slot, modules.get(slot));
            this.addSlot(new SlotItemHandler(
                    moduleHandler,
                    slot,
                    MODULE_START_X + slot * 18,
                    MODULE_Y) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return stack.getItem() instanceof ArmorModuleItem;
                }
            });
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack result = ItemStack.EMPTY;
        net.minecraft.world.inventory.Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();
        int moduleSlots = moduleHandler.getSlots();
        if (index < moduleSlots) {
            if (!this.moveItemStackTo(stackInSlot, moduleSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(stackInSlot.getItem() instanceof ArmorModuleItem)) {
            return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stackInSlot, 0, moduleSlots, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(final Player player) {
        ItemStack chestplate = ArmorModuleHelper.getManagedChestplate(playerInventory, chestSlot);
        return ArmorModuleHelper.acceptsModules(chestplate);
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    HOTBAR_Y));
        }
    }
}
