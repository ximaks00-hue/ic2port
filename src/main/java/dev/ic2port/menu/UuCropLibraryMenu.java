package dev.ic2port.menu;

import dev.ic2port.blockentity.UuCropLibraryBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class UuCropLibraryMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 120;
    private static final int HOTBAR_Y = 178;

    private final UuCropLibraryBlockEntity blockEntity;

    public UuCropLibraryMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public UuCropLibraryMenu(final int containerId, final Inventory playerInventory,
                               final UuCropLibraryBlockEntity blockEntity) {
        super(MenuTypeRegistry.UU_CROP_LIBRARY_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        for (int slot = 0; slot < UuCropLibraryBlockEntity.SLOT_COUNT; slot++) {
            int col = slot % 3;
            int row = slot / 3;
            addSlot(new SlotItemHandler(blockEntity.getSeedStorage(), slot, 62 + col * 18, 17 + row * 18));
        }
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    public UuCropLibraryBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && player.level().getBlockEntity(blockEntity.getBlockPos()) == blockEntity
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 0.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    private static UuCropLibraryBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof UuCropLibraryBlockEntity library) {
            return library;
        }
        throw new IllegalStateException("Expected UuCropLibraryBlockEntity at provided BlockPos");
    }
}
