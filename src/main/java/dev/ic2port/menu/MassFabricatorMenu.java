package dev.ic2port.menu;

import dev.ic2port.blockentity.MassFabricatorBlockEntity;
import dev.ic2port.item.ScrapItem;
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

public class MassFabricatorMenu extends MachineWithUpgradesMenu {

    private static final int DATA_FABRICATION_PROGRESS = 0;
    private static final int DATA_FABRICATION_MAX = 1;

    private final MassFabricatorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MassFabricatorMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(4));
    }

    public MassFabricatorMenu(
            final int containerId,
            final Inventory playerInventory,
            final MassFabricatorBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.MASS_FABRICATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(
                itemHandler,
                MassFabricatorBlockEntity.SLOT_SCRAP,
                56,
                17) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.getItem() instanceof ScrapItem;
            }
        });
        this.addSlot(new SlotItemHandler(
                itemHandler,
                MassFabricatorBlockEntity.SLOT_OUTPUT,
                116,
                35) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }
        });

        addUpgradeSlots(itemHandler, blockEntity.getProcessSlotCount());
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public boolean isFabricating() {
        return data.get(DATA_FABRICATION_PROGRESS) > 0;
    }

    public int getFabricationProgressScaled(final int width) {
        int progress = data.get(DATA_FABRICATION_PROGRESS);
        int max = data.get(DATA_FABRICATION_MAX);
        if (max <= 0 || progress <= 0) {
            return 0;
        }
        return Math.min(width, progress * width / max);
    }

    public int getEnergyScaled(final int height) {
        return getMachineEnergyScaled(data, height);
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

        int machineSlots = MassFabricatorBlockEntity.SLOT_COUNT;
        int upgradeStart = machineSlots;
        int upgradeEnd = machineSlots + MachineMenuLayout.UPGRADE_SLOT_COUNT;
        int playerStart = upgradeEnd;
        int playerEnd = this.slots.size();

        if (index < machineSlots) {
            if (!moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < upgradeEnd) {
            if (!moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)
                    && !moveItemStackTo(sourceStack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.getItem() instanceof ScrapItem) {
            if (!moveItemStackTo(sourceStack, MassFabricatorBlockEntity.SLOT_SCRAP, MassFabricatorBlockEntity.SLOT_SCRAP + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, upgradeStart, upgradeEnd, false)) {
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

    private static MassFabricatorBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof MassFabricatorBlockEntity fabricator) {
            return fabricator;
        }
        throw new IllegalStateException("Expected MassFabricatorBlockEntity at provided BlockPos");
    }
}
