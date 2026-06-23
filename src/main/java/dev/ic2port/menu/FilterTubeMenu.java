package dev.ic2port.menu;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Nine filter slots for the filter tube — slot index maps to an output direction.
 */
public class FilterTubeMenu extends AbstractContainerMenu {

    private static final int FILTER_SLOTS = 9;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final TubeBlockEntity tube;

    public FilterTubeMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public FilterTubeMenu(final int containerId, final Inventory playerInventory, final TubeBlockEntity tube) {
        super(MenuTypeRegistry.FILTER_TUBE_MENU.get(), containerId);
        this.tube = tube;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                int x = 62 + col * 18;
                int y = 17 + row * 18;
                this.addSlot(new SlotItemHandler(tube.getFilterHandler(), index, x, y) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        tube.syncToClientPublic();
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return tube.getRole() == TubeRole.FILTER
                && player.distanceToSqr(
                tube.getBlockPos().getX() + 0.5D,
                tube.getBlockPos().getY() + 0.5D,
                tube.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        quickMoved = source.copy();
        if (index < FILTER_SLOTS) {
            if (!moveItemStackTo(source, FILTER_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 0, FILTER_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (source.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, source);
        return quickMoved;
    }

    private static TubeBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof TubeBlockEntity tube) {
            return tube;
        }
        throw new IllegalStateException("Block entity is not a tube");
    }
}
