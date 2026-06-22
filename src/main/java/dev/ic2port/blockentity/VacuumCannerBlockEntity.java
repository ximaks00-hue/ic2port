package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.VacuumCannerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * MV vacuum canner — faster packaging at 16 EU/t (IC2 vacuum canner).
 */
public class VacuumCannerBlockEntity extends AbstractCannerBlockEntity {

    public static final double ENERGY_CAPACITY = 16_000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 16.0D;
    public static final int PROCESS_TIME = 25;

    public VacuumCannerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.VACUUM_CANNER_BE.get(), pos, state, ENERGY_CAPACITY);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected double getEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    @Override
    protected int getBaseProcessTime() {
        return PROCESS_TIME;
    }

    @Override
    protected Component getMenuTitle() {
        return Component.translatable("block.ic2port.vacuum_canner");
    }

    @Override
    protected boolean isVacuumCanner() {
        return true;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new VacuumCannerMenu(containerId, playerInventory, this, data);
    }
}
