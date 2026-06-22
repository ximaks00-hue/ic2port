package dev.ic2port.menu;

import dev.ic2port.blockentity.EsuBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.ItemEnergyHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class EsuMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final EsuBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public EsuMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public EsuMenu(final int containerId, final Inventory playerInventory,
                   final EsuBlockEntity blockEntity, final ContainerData data) {
        super(MenuTypeRegistry.ESU_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(itemHandler, EsuBlockEntity.SLOT_CHARGE, 56, 26) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return ItemEnergyHelper.canCharge(stack, EsuBlockEntity.TIER);
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, EsuBlockEntity.SLOT_DISCHARGE, 56, 53) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return ItemEnergyHelper.canDischargeInto(stack, EsuBlockEntity.TIER);
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(0);
        int max = data.get(1);
        if (max <= 0 || energy <= 0) return 0;
        return (int) ((long) energy * height / max);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        var slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = slot.getItem();
        ItemStack quickMoved = sourceStack.copy();

        if (index < 2) {
            if (!moveItemStackTo(sourceStack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (ItemEnergyHelper.canCharge(sourceStack, EsuBlockEntity.TIER)) {
            if (!moveItemStackTo(sourceStack, EsuBlockEntity.SLOT_CHARGE, EsuBlockEntity.SLOT_CHARGE + 1, false))
                return ItemStack.EMPTY;
        } else if (ItemEnergyHelper.canDischargeInto(sourceStack, EsuBlockEntity.TIER)) {
            if (!moveItemStackTo(sourceStack, EsuBlockEntity.SLOT_DISCHARGE, EsuBlockEntity.SLOT_DISCHARGE + 1, false))
                return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (sourceStack.getCount() == quickMoved.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, sourceStack);
        return quickMoved;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && level.getBlockEntity(blockEntity.getBlockPos()) == blockEntity
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 0.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    private static EsuBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof EsuBlockEntity esu) return esu;
        throw new IllegalStateException("Expected EsuBlockEntity at provided BlockPos");
    }
}
