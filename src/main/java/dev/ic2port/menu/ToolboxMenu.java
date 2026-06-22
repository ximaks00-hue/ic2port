package dev.ic2port.menu;

import dev.ic2port.item.ToolboxItem;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.ToolboxFilters;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Container for the portable toolbox item inventory.
 */
public class ToolboxMenu extends AbstractContainerMenu {

    private static final int TOOLBOX_ROWS = 2;
    private static final int TOOLBOX_COLS = 9;
    private static final int TOOLBOX_SLOT_COUNT = TOOLBOX_ROWS * TOOLBOX_COLS;
    private static final int TOOLBOX_START_Y = 18;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final Inventory playerInventory;
    private final int toolboxSlot;
    private final ItemStackHandler itemHandler;

    public ToolboxMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readVarInt());
    }

    public ToolboxMenu(final int containerId, final Inventory playerInventory, final int toolboxSlot) {
        super(MenuTypeRegistry.TOOLBOX_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.toolboxSlot = toolboxSlot;
        ItemStack toolboxStack = playerInventory.getItem(toolboxSlot);
        if (!(toolboxStack.getItem() instanceof ToolboxItem)) {
            throw new IllegalStateException("Toolbox slot does not contain a toolbox item");
        }
        this.itemHandler = ToolboxItem.createItemHandler(playerInventory, toolboxSlot);

        for (int row = 0; row < TOOLBOX_ROWS; row++) {
            for (int col = 0; col < TOOLBOX_COLS; col++) {
                int index = col + row * TOOLBOX_COLS;
                this.addSlot(new SlotItemHandler(
                        itemHandler,
                        index,
                        8 + col * 18,
                        TOOLBOX_START_Y + row * 18) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        return ToolboxFilters.isAllowed(stack);
                    }
                });
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        quickMoved = sourceStack.copy();

        if (index < TOOLBOX_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TOOLBOX_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ToolboxFilters.isAllowed(sourceStack)) {
            if (!moveItemStackTo(sourceStack, 0, TOOLBOX_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, TOOLBOX_SLOT_COUNT, this.slots.size(), false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, sourceStack);
        return quickMoved;
    }

    @Override
    public boolean stillValid(final Player player) {
        return !player.isRemoved()
                && playerInventory.getItem(toolboxSlot).getItem() instanceof ToolboxItem;
    }

    private void addPlayerInventory(final Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9 + 9;
                this.addSlot(createPlayerSlot(inventory, slotIndex, 8 + col * 18, PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory inventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(createPlayerSlot(inventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    private net.minecraft.world.inventory.Slot createPlayerSlot(
            final Inventory inventory,
            final int slotIndex,
            final int x,
            final int y) {
        return new net.minecraft.world.inventory.Slot(inventory, slotIndex, x, y) {
            @Override
            public boolean mayPickup(final Player player) {
                return slotIndex != toolboxSlot;
            }

            @Override
            public boolean mayPlace(final ItemStack stack) {
                return slotIndex != toolboxSlot;
            }
        };
    }
}
