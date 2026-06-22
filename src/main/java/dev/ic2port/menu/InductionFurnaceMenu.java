package dev.ic2port.menu;

import dev.ic2port.blockentity.InductionFurnaceBlockEntity;
import dev.ic2port.item.IUpgradeItem;
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

public class InductionFurnaceMenu extends MachineWithUpgradesMenu {

    private static final int LANE_A_INPUT_X = 44;
    private static final int LANE_A_OUTPUT_X = 116;
    private static final int LANE_B_INPUT_X = 44;
    private static final int LANE_B_OUTPUT_X = 116;
    private static final int LANE_A_Y = 26;
    private static final int LANE_B_Y = 44;

    private final InductionFurnaceBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public InductionFurnaceMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(6));
    }

    public InductionFurnaceMenu(
            final int containerId,
            final Inventory playerInventory,
            final InductionFurnaceBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.INDUCTION_FURNACE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();
        addLaneSlot(itemHandler, InductionFurnaceBlockEntity.SLOT_INPUT_A, LANE_A_INPUT_X, LANE_A_Y, true);
        addLaneSlot(itemHandler, InductionFurnaceBlockEntity.SLOT_OUTPUT_A, LANE_A_OUTPUT_X, LANE_A_Y, false);
        addLaneSlot(itemHandler, InductionFurnaceBlockEntity.SLOT_INPUT_B, LANE_B_INPUT_X, LANE_B_Y, true);
        addLaneSlot(itemHandler, InductionFurnaceBlockEntity.SLOT_OUTPUT_B, LANE_B_OUTPUT_X, LANE_B_Y, false);

        addUpgradeSlots(itemHandler);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    private void addLaneSlot(
            final IItemHandler itemHandler,
            final int slot,
            final int x,
            final int y,
            final boolean input) {
        if (input) {
            this.addSlot(createProcessInputSlot(itemHandler, slot, x, y));
            return;
        }
        this.addSlot(new SlotItemHandler(itemHandler, slot, x, y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }
        });
    }

    public int getProgressAScaled(final int width) {
        int max = data.get(1);
        if (max <= 0) {
            return 0;
        }
        return data.get(0) * width / max;
    }

    public int getProgressBScaled(final int width) {
        int max = data.get(3);
        if (max <= 0) {
            return 0;
        }
        return data.get(2) * width / max;
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(4);
        int max = data.get(5);
        if (max <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / max;
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

        final int machineSlots = 4;
        final int upgradeStart = machineSlots;
        final int upgradeEnd = machineSlots + MachineMenuLayout.UPGRADE_SLOT_COUNT;
        final int playerStart = upgradeEnd;
        final int playerEnd = this.slots.size();

        if (index < machineSlots) {
            if (!this.moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < upgradeEnd) {
            if (!this.moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)
                    && !this.moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.getItem() instanceof IUpgradeItem) {
            if (!this.moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)
                    && !this.moveItemStackTo(sourceStack, InductionFurnaceBlockEntity.SLOT_INPUT_A, InductionFurnaceBlockEntity.SLOT_INPUT_A + 1, false)
                    && !this.moveItemStackTo(sourceStack, InductionFurnaceBlockEntity.SLOT_INPUT_B, InductionFurnaceBlockEntity.SLOT_INPUT_B + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, InductionFurnaceBlockEntity.SLOT_INPUT_A, InductionFurnaceBlockEntity.SLOT_INPUT_A + 1, false)
                && !this.moveItemStackTo(sourceStack, InductionFurnaceBlockEntity.SLOT_INPUT_B, InductionFurnaceBlockEntity.SLOT_INPUT_B + 1, false)) {
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

    private static InductionFurnaceBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof InductionFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected InductionFurnaceBlockEntity at provided BlockPos");
    }
}
