package dev.ic2port.menu;

import dev.ic2port.item.AutoFeederModuleItem;
import dev.ic2port.item.StorageArmorModuleItem;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class AutoFeederMenu extends AbstractContainerMenu {

    private static final int MODULE_START_X = 62;
    private static final int MODULE_Y = 20;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Inventory playerInventory;
    private final int moduleSlot;
    private final ItemStackHandler itemHandler;

    public AutoFeederMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readVarInt());
    }

    public AutoFeederMenu(final int containerId, final Inventory playerInventory, final int moduleSlot) {
        super(MenuTypeRegistry.AUTO_FEEDER_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.moduleSlot = moduleSlot;
        ItemStack moduleStack = playerInventory.getItem(moduleSlot);
        if (!(moduleStack.getItem() instanceof AutoFeederModuleItem module)) {
            throw new IllegalStateException("Module slot does not contain an auto feeder module");
        }
        this.itemHandler = module.createItemHandler(playerInventory, moduleSlot);

        for (int slot = 0; slot < AutoFeederModuleItem.SLOT_COUNT; slot++) {
            this.addSlot(new SlotItemHandler(
                    itemHandler,
                    slot,
                    MODULE_START_X + slot * 18,
                    MODULE_Y) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return stack.is(ItemRegistry.FILLED_TIN_CAN.get());
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
        int moduleSlots = AutoFeederModuleItem.SLOT_COUNT;
        if (index < moduleSlots) {
            if (!this.moveItemStackTo(stackInSlot, moduleSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!stackInSlot.is(ItemRegistry.FILLED_TIN_CAN.get())) {
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
        ItemStack held = playerInventory.getItem(moduleSlot);
        return held.getItem() instanceof AutoFeederModuleItem;
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
