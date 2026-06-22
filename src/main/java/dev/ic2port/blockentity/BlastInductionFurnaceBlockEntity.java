package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.InductionFurnaceMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.OreInputHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * MV blast induction furnace — ores only, 4× lane speed at 30 EU/t.
 */
public class BlastInductionFurnaceBlockEntity extends InductionFurnaceBlockEntity {

    public static final int TIER = EnergyTier.MV;

    public BlastInductionFurnaceBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.BLAST_INDUCTION_FURNACE_BE.get(), pos, state);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected int getSpeedDivisor() {
        return 4;
    }

    @Override
    protected double getFallbackEnergyPerTick() {
        return 30.0D;
    }

    @Override
    protected int getFallbackProcessingTime() {
        return 40;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return OreInputHelper.isOreInput(stack) && super.isValidProcessInput(stack);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.blast_induction_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new InductionFurnaceMenu(containerId, playerInventory, this, getContainerData());
    }
}
