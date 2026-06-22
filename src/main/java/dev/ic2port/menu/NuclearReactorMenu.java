package dev.ic2port.menu;

import dev.ic2port.blockentity.NuclearReactorBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.ReactorGridHelper;
import dev.ic2port.util.ReactorItemFilters;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class NuclearReactorMenu extends AbstractContainerMenu {

    private static final int GRID_START_Y = 18;
    private static final int PLAYER_INVENTORY_START_Y = 140;
    private static final int HOTBAR_Y = 198;

    private static final int DATA_HEAT = 0;
    private static final int DATA_MAX_HEAT = 1;
    private static final int DATA_STORED_ENERGY = 2;
    private static final int DATA_MAX_ENERGY = 3;
    private static final int DATA_CHAMBER_COUNT = 4;
    private static final int DATA_ACTIVE = 5;

    private final NuclearReactorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public NuclearReactorMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(6));
    }

    public NuclearReactorMenu(
            final int containerId,
            final Inventory playerInventory,
            final NuclearReactorBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.NUCLEAR_REACTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .orElseThrow(() -> new IllegalStateException("Nuclear reactor item handler capability is missing"));

        for (int row = 0; row < NuclearReactorBlockEntity.GRID_HEIGHT; row++) {
            for (int col = 0; col < NuclearReactorBlockEntity.GRID_WIDTH; col++) {
                int index = NuclearReactorBlockEntity.toSlotIndex(col, row);
                final int column = col;
                this.addSlot(new SlotItemHandler(
                        itemHandler,
                        index,
                        8 + col * 18,
                        GRID_START_Y + row * 18) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        return isColumnEnabled(column) && ReactorItemFilters.isAllowedInReactor(stack);
                    }

                    @Override
                    public boolean mayPickup(final Player player) {
                        return isColumnEnabled(column);
                    }
                });
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getHeatScaled(final int height) {
        int heat = data.get(DATA_HEAT);
        int maxHeat = data.get(DATA_MAX_HEAT);
        if (maxHeat <= 0 || heat <= 0) {
            return 0;
        }
        return Math.min(height, heat * height / maxHeat);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(DATA_STORED_ENERGY);
        int maxEnergy = data.get(DATA_MAX_ENERGY);
        if (maxEnergy <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / maxEnergy;
    }

    public int getHeat() {
        return data.get(DATA_HEAT);
    }

    public int getMaxHeat() {
        return data.get(DATA_MAX_HEAT);
    }

    public int getStoredEnergy() {
        return data.get(DATA_STORED_ENERGY);
    }

    public int getMaxEnergy() {
        return data.get(DATA_MAX_ENERGY);
    }

    public boolean isActive() {
        return data.get(DATA_ACTIVE) != 0;
    }

    private boolean isColumnEnabled(final int column) {
        int chamberCount = data.get(DATA_CHAMBER_COUNT);
        return ReactorGridHelper.isColumnEnabled(column, chamberCount);
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
        int reactorSlots = NuclearReactorBlockEntity.SLOT_COUNT;

        if (index < reactorSlots) {
            if (!moveItemStackTo(sourceStack, reactorSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ReactorItemFilters.isAllowedInReactor(sourceStack)) {
            if (!moveToEnabledReactorSlot(sourceStack)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, reactorSlots, this.slots.size(), false)) {
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

    private boolean moveToEnabledReactorSlot(final ItemStack stack) {
        for (int slot = 0; slot < NuclearReactorBlockEntity.SLOT_COUNT; slot++) {
            if (!isColumnEnabled(NuclearReactorBlockEntity.slotToX(slot))) {
                continue;
            }
            if (moveItemStackTo(stack, slot, slot + 1, false)) {
                return true;
            }
        }
        return false;
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

    private static NuclearReactorBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof NuclearReactorBlockEntity reactor) {
            return reactor;
        }
        throw new IllegalStateException("Expected NuclearReactorBlockEntity at provided BlockPos");
    }
}
