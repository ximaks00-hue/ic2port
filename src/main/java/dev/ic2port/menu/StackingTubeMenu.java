package dev.ic2port.menu;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.ContainerDataHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Displays stacking buffer state and allows manual extraction.
 */
public class StackingTubeMenu extends AbstractContainerMenu {

    private static final int HOTBAR_Y = 142;

    private final TubeBlockEntity tube;
    private final ContainerData data;

    public StackingTubeMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public StackingTubeMenu(final int containerId, final Inventory playerInventory, final TubeBlockEntity tube) {
        super(MenuTypeRegistry.STACKING_TUBE_MENU.get(), containerId);
        this.tube = tube;
        this.data = new ContainerData() {
            @Override
            public int get(final int index) {
                return switch (index) {
                    case 0 -> tube.getStackBufferCount();
                    case 1 -> tube.getStackingThreshold();
                    default -> 0;
                };
            }

            @Override
            public void set(final int index, final int value) {
                ContainerDataHelper.ignoreClientWrite();
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        addDataSlots(data);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    public int getStoredCount() {
        return data.get(0);
    }

    public int getThreshold() {
        return data.get(1);
    }

    public void extractBuffer() {
        if (tube.getLevel() != null && !tube.getLevel().isClientSide) {
            tube.forceEjectStackBuffer();
        }
    }

    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (id == 0) {
            tube.forceEjectStackBuffer();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(final Player player) {
        return tube.getRole() == TubeRole.STACKING
                && player.distanceToSqr(
                tube.getBlockPos().getX() + 0.5D,
                tube.getBlockPos().getY() + 0.5D,
                tube.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    private static TubeBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof TubeBlockEntity tube) {
            return tube;
        }
        throw new IllegalStateException("Block entity is not a tube");
    }
}
