package dev.ic2port.menu;

import dev.ic2port.blockentity.PesuBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PesuMenu extends EsuMenu {

    public PesuMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(2));
    }

    public PesuMenu(final int containerId, final Inventory playerInventory,
                    final PesuBlockEntity blockEntity, final ContainerData data) {
        super(MenuTypeRegistry.PESU_MENU.get(), containerId, playerInventory, blockEntity, data);
    }

    private static PesuBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof PesuBlockEntity pesu) {
            return pesu;
        }
        throw new IllegalStateException("Expected PesuBlockEntity at provided BlockPos");
    }
}
