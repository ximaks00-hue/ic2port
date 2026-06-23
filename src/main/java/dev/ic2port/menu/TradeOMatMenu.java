package dev.ic2port.menu;

import dev.ic2port.blockentity.TradeOMatBlockEntity;
import dev.ic2port.item.TradeCoinItem;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TradeOMatMenu extends AbstractContainerMenu {

    private final TradeOMatBlockEntity blockEntity;
    private final ContainerData data;
    private boolean buyerView;

    public TradeOMatMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(3));
    }

    public TradeOMatMenu(
            final int containerId,
            final Inventory playerInventory,
            final TradeOMatBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.TRADE_O_MAT_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;
        this.buyerView = blockEntity.isBuyerView(playerInventory.player);

        IItemHandler handler = blockEntity.getFullItemHandler();
        this.addSlot(new SlotItemHandler(handler, TradeOMatBlockEntity.SLOT_OFFER, 44, 35) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return !buyerView;
            }
        });
        this.addSlot(new SlotItemHandler(handler, TradeOMatBlockEntity.SLOT_PAYMENT, 80, 35) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.getItem() instanceof TradeCoinItem;
            }
        });
        this.addSlot(new SlotItemHandler(handler, TradeOMatBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public void setBuyerView(final boolean buyerView) {
        this.buyerView = buyerView;
    }

    public boolean isBuyerView() {
        return buyerView;
    }

    public int getPrice() {
        return data.get(0);
    }

    public boolean hasLinkedChest() {
        return data.get(1) != 0;
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && blockEntity.getLevel() == player.level()
                && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    private static TradeOMatBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof TradeOMatBlockEntity mat) {
            return mat;
        }
        throw new IllegalStateException("Trade-O-Mat block entity not found");
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, MachineMenuLayout.HOTBAR_Y));
        }
    }
}
