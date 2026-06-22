package dev.ic2port.menu;

import dev.ic2port.blockentity.ThermalCentrifugeBlockEntity;
import dev.ic2port.item.CentrifugeRotorItem;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ThermalCentrifugeMenu extends MachineWithUpgradesMenu {

    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int ROTOR_SLOT_X = 8;
    private static final int ROTOR_SLOT_Y = 53;
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_START_Y = 17;

    private final ThermalCentrifugeBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ThermalCentrifugeMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(6));
    }

    public ThermalCentrifugeMenu(
            final int containerId,
            final Inventory playerInventory,
            final ThermalCentrifugeBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.THERMAL_CENTRIFUGE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();

        this.addSlot(createProcessInputSlot(
                itemHandler,
                ThermalCentrifugeBlockEntity.SLOT_INPUT,
                MachineMenuLayout.SLOT_INPUT_X,
                MachineMenuLayout.SLOT_INPUT_Y));

        for (int index = 0; index < 3; index++) {
            final int slotIndex = ThermalCentrifugeBlockEntity.SLOT_OUTPUT_PRIMARY + index;
            this.addSlot(new SlotItemHandler(
                    itemHandler,
                    slotIndex,
                    OUTPUT_SLOT_X,
                    OUTPUT_SLOT_START_Y + index * 18) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return false;
                }
            });
        }

        this.addSlot(new SlotItemHandler(
                itemHandler,
                ThermalCentrifugeBlockEntity.SLOT_ROTOR,
                ROTOR_SLOT_X,
                ROTOR_SLOT_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return itemHandler.isItemValid(ThermalCentrifugeBlockEntity.SLOT_ROTOR, stack);
            }
        });

        addUpgradeSlots(itemHandler, MACHINE_SLOT_COUNT);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    protected void addUpgradeSlots(final IItemHandler itemHandler, final int machineSlotCount) {
        for (int index = 0; index < MachineMenuLayout.UPGRADE_SLOT_COUNT; index++) {
            final int slotIndex = machineSlotCount + index;
            final int y = MachineMenuLayout.UPGRADE_SLOT_START_Y + index * MachineMenuLayout.UPGRADE_SLOT_SPACING;
            this.addSlot(new SlotItemHandler(itemHandler, slotIndex, MachineMenuLayout.UPGRADE_SLOT_X, y) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return stack.getItem() instanceof dev.ic2port.item.IUpgradeItem;
                }
            });
        }
    }

    public boolean isCrafting() {
        return isMachineCrafting(data);
    }

    public boolean isHeating() {
        return blockEntity.isHeating();
    }

    public int getProcessedProgressScaled(final int width) {
        return getMachineProgressScaled(data, width);
    }

    public int getEnergyScaled(final int height) {
        return getMachineEnergyScaled(data, height);
    }

    public int getRotorHeatScaled(final int height) {
        int heat = data.get(ThermalCentrifugeBlockEntity.DATA_ROTOR_HEAT);
        int maxHeat = data.get(ThermalCentrifugeBlockEntity.DATA_MAX_ROTOR_HEAT);
        if (maxHeat <= 0 || heat <= 0) {
            return 0;
        }
        return heat * height / maxHeat;
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

        int upgradeStart = MACHINE_SLOT_COUNT;
        int upgradeEnd = upgradeStart + MachineMenuLayout.UPGRADE_SLOT_COUNT;
        int playerStart = upgradeEnd;
        int playerEnd = this.slots.size();

        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < upgradeEnd) {
            if (!moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)
                    && !moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, ThermalCentrifugeBlockEntity.SLOT_INPUT, ThermalCentrifugeBlockEntity.SLOT_INPUT + 1, false)
                && !(CentrifugeRotorItem.isRotorStack(sourceStack)
                && moveItemStackTo(sourceStack, ThermalCentrifugeBlockEntity.SLOT_ROTOR, ThermalCentrifugeBlockEntity.SLOT_ROTOR + 1, false))
                && !moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)) {
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
                        MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18
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
                    MachineMenuLayout.HOTBAR_Y
            ));
        }
    }

    private static ThermalCentrifugeBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof ThermalCentrifugeBlockEntity centrifuge) {
            return centrifuge;
        }
        throw new IllegalStateException("Expected ThermalCentrifugeBlockEntity at provided BlockPos");
    }
}
