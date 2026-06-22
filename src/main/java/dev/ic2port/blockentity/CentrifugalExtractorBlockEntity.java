package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.CentrifugalExtractorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * MV centrifugal extractor — 2× speed vs standard extractor at 16 EU/t.
 */
public class CentrifugalExtractorBlockEntity extends ExtractorBlockEntity {

    public static final int TIER = EnergyTier.MV;

    public CentrifugalExtractorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CENTRIFUGAL_EXTRACTOR_BE.get(), pos, state);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected int getProcessTimeDivisor() {
        return 2;
    }

    @Override
    protected double getFallbackEnergyPerTick() {
        return 16.0D;
    }

    @Override
    protected int getFallbackProcessingTime() {
        return 100;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.centrifugal_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new CentrifugalExtractorMenu(containerId, playerInventory, this, getContainerData());
    }
}
