package dev.ic2port.blockentity;

import dev.ic2port.block.PesuBlock;
import dev.ic2port.menu.PesuMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Power Energy Storage Unit — EV tier, stores up to 100 million EU.
 */
public class PesuBlockEntity extends EsuBlockEntity {

    public static final double ENERGY_CAPACITY = 100_000_000.0D;
    public static final double MAX_OUTPUT_PER_TICK = 8192.0D;

    public PesuBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.PESU_BE.get(), pos, state,
                ENERGY_CAPACITY, MAX_OUTPUT_PER_TICK, PesuBlock.FACING,
                Component.translatable("block.ic2port.pesu"));
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final PesuBlockEntity pesu) {
        pesu.tickServer();
    }

    @Override
    protected AbstractContainerMenu createStorageMenu(
            final int containerId,
            final Inventory playerInventory,
            final ContainerData containerData) {
        return new PesuMenu(containerId, playerInventory, this, containerData);
    }
}
