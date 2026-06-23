package dev.ic2port.blockentity;

import dev.ic2port.block.IsuBlock;
import dev.ic2port.menu.IsuMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Industrial Storage Unit — EV tier, stores up to 1 billion EU.
 */
public class IsuBlockEntity extends EsuBlockEntity {

    public static final double ENERGY_CAPACITY = 1_000_000_000.0D;
    public static final double MAX_OUTPUT_PER_TICK = 32_768.0D;

    public IsuBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ISU_BE.get(), pos, state,
                ENERGY_CAPACITY, MAX_OUTPUT_PER_TICK, IsuBlock.FACING,
                Component.translatable("block.ic2port.isu"));
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                  final IsuBlockEntity isu) {
        isu.tickServer();
    }

    @Override
    protected AbstractContainerMenu createStorageMenu(
            final int containerId,
            final Inventory playerInventory,
            final ContainerData containerData) {
        return new IsuMenu(containerId, playerInventory, this, containerData);
    }
}
