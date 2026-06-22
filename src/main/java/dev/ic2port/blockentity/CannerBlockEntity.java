package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.CannerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * LV canner — refills electric tools using consumables at 1 EU/t.
 */
public class CannerBlockEntity extends AbstractCannerBlockEntity {

    public static final double ENERGY_CAPACITY = 4000.0D;
    public static final int TIER = EnergyTier.LV;
    public static final double ENERGY_PER_TICK = 1.0D;
    public static final int PROCESS_TIME = 100;

    public CannerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CANNER_BE.get(), pos, state, ENERGY_CAPACITY);
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
        return Component.translatable("block.ic2port.canner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new CannerMenu(containerId, playerInventory, this, data);
    }
}
