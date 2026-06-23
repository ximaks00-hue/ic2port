package dev.ic2port.menu;

import dev.ic2port.blockentity.IsuBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public class IsuMenu extends EsuMenu {

    public IsuMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public IsuMenu(final int containerId, final Inventory playerInventory,
                   final IsuBlockEntity blockEntity, final ContainerData data) {
        super(MenuTypeRegistry.ISU_MENU.get(), containerId, playerInventory, blockEntity, data);
    }

    private static IsuBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof IsuBlockEntity isu) {
            return isu;
        }
        throw new IllegalStateException("Expected IsuBlockEntity at provided BlockPos");
    }
}
