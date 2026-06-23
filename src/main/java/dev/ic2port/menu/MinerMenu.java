package dev.ic2port.menu;

import dev.ic2port.blockentity.MinerBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MinerHelper;
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

public class MinerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START = 12;
    private static final int PLAYER_INVENTORY_END = 39;

    private final MinerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MinerMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(4));
    }

    public MinerMenu(
            final int containerId,
            final Inventory playerInventory,
            final MinerBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.MINER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(handler, MinerBlockEntity.SLOT_DRILL, 38, 17) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return MinerHelper.isValidDrill(stack);
            }
        });
        this.addSlot(new SlotItemHandler(handler, MinerBlockEntity.SLOT_SCANNER, 56, 17) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return MinerHelper.isValidScanner(stack);
            }
        });
        this.addSlot(new SlotItemHandler(handler, MinerBlockEntity.SLOT_PIPE, 74, 17) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return MinerHelper.isMiningPipe(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = MinerBlockEntity.SLOT_OUTPUT_START + row * 3 + col;
                this.addSlot(new SlotItemHandler(handler, slot, 110 + col * 18, 17 + row * 18) {
                    @Override
                    public boolean mayPlace(final ItemStack stack) {
                        return false;
                    }
                });
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(0);
        int maxEnergy = data.get(1);
        if (maxEnergy <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / maxEnergy;
    }

    public boolean isDone() {
        return data.get(2) == 1;
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

        if (index < PLAYER_INVENTORY_START) {
            if (!moveItemStackTo(sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (MinerHelper.isValidDrill(sourceStack)) {
            if (!moveItemStackTo(sourceStack, MinerBlockEntity.SLOT_DRILL, MinerBlockEntity.SLOT_DRILL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (MinerHelper.isValidScanner(sourceStack)) {
            if (!moveItemStackTo(sourceStack, MinerBlockEntity.SLOT_SCANNER, MinerBlockEntity.SLOT_SCANNER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (MinerHelper.isMiningPipe(sourceStack)) {
            if (!moveItemStackTo(sourceStack, MinerBlockEntity.SLOT_PIPE, MinerBlockEntity.SLOT_PIPE + 1, false)) {
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
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static MinerBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof MinerBlockEntity miner) {
            return miner;
        }
        throw new IllegalStateException("Expected MinerBlockEntity at provided BlockPos");
    }
}
