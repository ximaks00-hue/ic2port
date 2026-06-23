package dev.ic2port.menu;

import dev.ic2port.blockentity.InductionMatrixBlockEntity;
import dev.ic2port.item.CapacitorCellItem;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class InductionMatrixMenu extends AbstractContainerMenu {

    private static final int CELL_GRID_SIZE = 3;
    private static final int CELL_SLOTS = CELL_GRID_SIZE * CELL_GRID_SIZE;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;
    private static final int DATA_STRUCTURE = 2;

    private final InductionMatrixBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public InductionMatrixMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(3));
    }

    public InductionMatrixMenu(
            final int containerId,
            final Inventory playerInventory,
            final InductionMatrixBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.INDUCTION_MATRIX_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getCellItemHandler();

        int gridStartX = 62;
        int gridStartY = 17;
        for (int row = 0; row < CELL_GRID_SIZE; row++) {
            for (int col = 0; col < CELL_GRID_SIZE; col++) {
                int index = col + row * CELL_GRID_SIZE;
                this.addSlot(new SlotItemHandler(
                        itemHandler,
                        index,
                        gridStartX + col * 18,
                        gridStartY + row * 18) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        return stack.is(ItemRegistry.CAPACITOR_CELL.get());
                    }
                });
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(DATA_ENERGY);
        int capacity = data.get(DATA_CAPACITY);
        if (capacity <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / capacity;
    }

    public int getStoredEnergy() {
        return data.get(DATA_ENERGY);
    }

    public int getEnergyCapacity() {
        return data.get(DATA_CAPACITY);
    }

    public boolean isStructureValid() {
        return data.get(DATA_STRUCTURE) != 0;
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

        if (index < CELL_SLOTS) {
            if (!moveItemStackTo(sourceStack, CELL_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.getItem() instanceof CapacitorCellItem) {
            if (!moveItemStackTo(sourceStack, 0, CELL_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, CELL_SLOTS, this.slots.size(), false)) {
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
        return this.blockEntity != null
                && this.level.getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity
                && player.distanceToSqr(
                this.blockEntity.getBlockPos().getX() + 0.5D,
                this.blockEntity.getBlockPos().getY() + 0.5D,
                this.blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        PLAYER_INVENTORY_START_Y + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    HOTBAR_Y
            ));
        }
    }

    private static InductionMatrixBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof InductionMatrixBlockEntity matrix) {
            return matrix;
        }
        throw new IllegalStateException("Expected InductionMatrixBlockEntity at provided BlockPos");
    }
}
