package dev.ic2port.menu;

import dev.ic2port.blockentity.FusionReactorBlockEntity;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.FusionMeltableHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class FusionReactorMenu extends AbstractContainerMenu {

    private static final int FUEL_START_X = 26;
    private static final int FUEL_Y = 35;
    private static final int MELTABLE_X = 134;
    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final FusionReactorBlockEntity blockEntity;
    private final ContainerData data;

    public FusionReactorMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                getBlockEntity(playerInventory, extraData),
                new SimpleContainerData(9));
    }

    public FusionReactorMenu(
            final int containerId,
            final Inventory playerInventory,
            final FusionReactorBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.FUSION_REACTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        for (int slot = FusionReactorBlockEntity.FUEL_SLOT_START;
                slot <= FusionReactorBlockEntity.FUEL_SLOT_END;
                slot++) {
            this.addSlot(new SlotItemHandler(
                    blockEntity.getFullItemHandler(),
                    slot,
                    FUEL_START_X + (slot - FusionReactorBlockEntity.FUEL_SLOT_START) * 18,
                    FUEL_Y));
        }
        this.addSlot(new SlotItemHandler(
                blockEntity.getFullItemHandler(),
                FusionReactorBlockEntity.MELTABLE_SLOT,
                MELTABLE_X,
                FUEL_Y));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public boolean isStructureValid() {
        return data.get(4) == 1;
    }

    public int getHeatScaled(final int height) {
        int max = data.get(1);
        return max <= 0 ? 0 : data.get(0) * height / max;
    }

    public int getLavaScaled(final int height) {
        int max = data.get(3);
        return max <= 0 ? 0 : data.get(2) * height / max;
    }

    public int getEnergyScaled(final int height) {
        int max = data.get(6);
        return max <= 0 ? 0 : data.get(5) * height / max;
    }

    public int getHeat() {
        return data.get(0);
    }

    public int getMaxHeat() {
        return data.get(1);
    }

    public int getLavaMb() {
        return data.get(2);
    }

    public int getMaxLavaMb() {
        return data.get(3);
    }

    public int getStoredEnergy() {
        return data.get(5);
    }

    public int getMaxEnergy() {
        return data.get(6);
    }

    public boolean isHeated() {
        return getMaxHeat() > 0 && getHeat() >= getMaxHeat();
    }

    public boolean isComparatorHeatMode() {
        return data.get(7) == 1;
    }

    public boolean isAutoExportLava() {
        return data.get(8) == 1;
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
        int machineSlots = FusionReactorBlockEntity.SLOT_COUNT;
        if (index < machineSlots) {
            if (!this.moveItemStackTo(stackInSlot, machineSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.is(ItemRegistry.FUEL_ROD.get()) || stackInSlot.is(ItemRegistry.MOX_FUEL_ROD.get())) {
            if (!this.moveItemStackTo(stackInSlot, 0, FusionReactorBlockEntity.FUEL_SLOT_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (FusionMeltableHelper.isMeltable(stackInSlot)) {
            if (!this.moveItemStackTo(stackInSlot, FusionReactorBlockEntity.MELTABLE_SLOT,
                    FusionReactorBlockEntity.MELTABLE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        return blockEntity != null
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 0.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
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

    private static FusionReactorBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof FusionReactorBlockEntity reactor) {
            return reactor;
        }
        throw new IllegalStateException("Expected fusion reactor block entity");
    }
}
