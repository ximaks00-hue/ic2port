package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.UuExpansionMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Matter expansion — selects one of nine UU-Matter recipes for more efficient duplication.
 */
public class UuExpansionBlockEntity extends BaseMachineBlockEntity implements MenuProvider {

    public static final int RECIPE_SLOT_COUNT = 9;
    public static final double UU_PER_OPERATION = 1.0D;
    public static final double EU_PER_OPERATION = 50_000.0D;

    private int selectedRecipe;

    public UuExpansionBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.UU_EXPANSION_BE.get(), pos, state, RECIPE_SLOT_COUNT + 2, 500_000.0D);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final UuExpansionBlockEntity expansion) {
        expansion.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || !isServerProcessingEnabled()) {
            return;
        }
        ItemStack uuInput = getItemHandler().getStackInSlot(RECIPE_SLOT_COUNT);
        ItemStack outputSlot = getItemHandler().getStackInSlot(RECIPE_SLOT_COUNT + 1);
        if (uuInput.isEmpty() || !uuInput.is(ItemRegistry.UU_MATTER.get()) || !outputSlot.isEmpty()) {
            return;
        }
        ItemStack recipe = getItemHandler().getStackInSlot(selectedRecipe);
        if (recipe.isEmpty() || !consumeEnergy(EU_PER_OPERATION)) {
            return;
        }
        uuInput.shrink(1);
        getItemHandler().setStackInSlot(RECIPE_SLOT_COUNT + 1, recipe.copyWithCount(1));
        setChanged();
    }

    @Override
    public int getTier() {
        return EnergyTier.EV;
    }

    public int getSelectedRecipe() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(final int index) {
        selectedRecipe = Math.floorMod(index, RECIPE_SLOT_COUNT);
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.uu_expansion");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new UuExpansionMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("SelectedRecipe", selectedRecipe);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        selectedRecipe = Math.floorMod(tag.getInt("SelectedRecipe"), RECIPE_SLOT_COUNT);
    }
}
