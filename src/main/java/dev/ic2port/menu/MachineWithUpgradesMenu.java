package dev.ic2port.menu;

import dev.ic2port.item.IUpgradeItem;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Base container for IC2 machines with four player-only upgrade slots.
 */
public abstract class MachineWithUpgradesMenu extends AbstractContainerMenu {

    protected MachineWithUpgradesMenu(@Nullable final MenuType<?> menuType, final int containerId) {
        super(menuType, containerId);
    }

    protected void addUpgradeSlots(final IItemHandler itemHandler) {
        for (int index = 0; index < MachineMenuLayout.UPGRADE_SLOT_COUNT; index++) {
            final int slotIndex = MachineMenuLayout.upgradeSlotIndex(index);
            final int y = MachineMenuLayout.UPGRADE_SLOT_START_Y + index * MachineMenuLayout.UPGRADE_SLOT_SPACING;
            this.addSlot(new SlotItemHandler(itemHandler, slotIndex, MachineMenuLayout.UPGRADE_SLOT_X, y) {
                @Override
                public boolean mayPlace(final ItemStack stack) {
                    return stack.getItem() instanceof IUpgradeItem;
                }
            });
        }
    }

    protected SlotItemHandler createProcessInputSlot(
            final IItemHandler itemHandler,
            final int slotIndex,
            final int x,
            final int y) {
        return new SlotItemHandler(itemHandler, slotIndex, x, y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return itemHandler.isItemValid(slotIndex, stack);
            }
        };
    }

    protected boolean isMachineCrafting(final ContainerData data) {
        return data.get(MachineMenuLayout.DATA_PROGRESS) > 0;
    }

    protected int getMachineProgressScaled(final ContainerData data, final int width) {
        int progress = data.get(MachineMenuLayout.DATA_PROGRESS);
        int maxProgress = data.get(MachineMenuLayout.DATA_MAX_PROGRESS);
        if (maxProgress <= 0 || progress <= 0) {
            return 0;
        }
        return progress * width / maxProgress;
    }

    protected int getMachineEnergyScaled(final ContainerData data, final int height) {
        int energy = data.get(MachineMenuLayout.DATA_STORED_ENERGY);
        int maxEnergy = data.get(MachineMenuLayout.DATA_MAX_ENERGY);
        if (maxEnergy <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / maxEnergy;
    }

    protected ItemStack quickMoveMachineStack(final Player player, final int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        quickMoved = sourceStack.copy();

        int machineSlots = MachineMenuLayout.MACHINE_SLOT_COUNT;
        int upgradeStart = machineSlots;
        int upgradeEnd = MachineMenuLayout.playerInventoryStartIndex();
        int playerStart = upgradeEnd;
        int playerEnd = this.slots.size();

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
                    && !this.moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
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
}
