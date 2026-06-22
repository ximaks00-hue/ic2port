package dev.ic2port.menu;

import dev.ic2port.blockentity.MFEBlockEntity;
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
import dev.ic2port.util.ItemEnergyHelper;
import net.minecraftforge.items.SlotItemHandler;

public class MFEMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int SLOT_CHARGE_X = 56;
    private static final int SLOT_CHARGE_Y = 26;
    private static final int SLOT_DISCHARGE_X = 56;
    private static final int SLOT_DISCHARGE_Y = 53;

    private static final int DATA_STORED_ENERGY = 0;
    private static final int DATA_MAX_ENERGY = 1;

    private final MFEBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MFEMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public MFEMenu(
            final int containerId,
            final Inventory playerInventory,
            final MFEBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.MFE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(itemHandler, MFEBlockEntity.SLOT_CHARGE, SLOT_CHARGE_X, SLOT_CHARGE_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return ItemEnergyHelper.canCharge(stack, MFEBlockEntity.TIER);
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, MFEBlockEntity.SLOT_DISCHARGE, SLOT_DISCHARGE_X, SLOT_DISCHARGE_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return ItemEnergyHelper.canDischargeInto(stack, MFEBlockEntity.TIER);
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(DATA_STORED_ENERGY);
        int maxEnergy = data.get(DATA_MAX_ENERGY);
        if (maxEnergy <= 0 || energy <= 0) {
            return 0;
        }
        return (int) ((long) energy * height / maxEnergy);
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

        if (index < 2) {
            if (!moveItemStackTo(sourceStack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.canCharge(sourceStack, MFEBlockEntity.TIER)) {
            if (!moveItemStackTo(sourceStack, MFEBlockEntity.SLOT_CHARGE, MFEBlockEntity.SLOT_CHARGE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ItemEnergyHelper.canDischargeInto(sourceStack, MFEBlockEntity.TIER)) {
            if (!moveItemStackTo(sourceStack, MFEBlockEntity.SLOT_DISCHARGE, MFEBlockEntity.SLOT_DISCHARGE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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

    private static MFEBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof MFEBlockEntity mfe) {
            return mfe;
        }
        throw new IllegalStateException("Expected MFEBlockEntity at provided BlockPos");
    }
}
