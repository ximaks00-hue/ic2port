package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.MaceratorMenu;
import dev.ic2port.recipe.MaceratorRecipe;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.RecipeTypeRegistry;
import dev.ic2port.util.MachineRecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * LV macerator — consumes EU and grinds items using {@link MaceratorRecipe} entries.
 */
public class MaceratorBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 4000.0D;
    public static final int TIER = EnergyTier.LV;
    public static final double ENERGY_PER_TICK = 2.0D;
    public static final int DEFAULT_PROCESSING_TIME = 300;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private int progress;
    private int maxProgress = DEFAULT_PROCESSING_TIME;
    @Nullable
    private ResourceLocation activeRecipeId;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> (int) Math.round(getStoredEnergy());
                case 3 -> (int) Math.round(getCapacity());
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MaceratorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MACERATOR_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level,
                RecipeTypeRegistry.MACERATOR.get(),
                MaceratorRecipe.class,
                stack,
                MaceratorRecipe::getInput);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MaceratorBlockEntity macerator) {
        macerator.tickServer();
    }

    private void tickServer() {
        if (!isServerProcessingEnabled()) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            progress = 0;
        }

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        ItemStack output = getItemHandler().getStackInSlot(SLOT_OUTPUT);

        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<MaceratorRecipe> recipeOptional = resolveActiveRecipe(input);
        if (recipeOptional.isEmpty()) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            setChanged();
            return;
        }

        MaceratorRecipe recipe = recipeOptional.get();
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, activeRecipeId, progress)) {
            progress = 0;
        }
        int baseProcessTime = recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : DEFAULT_PROCESSING_TIME;
        maxProgress = getScaledProcessTime(baseProcessTime);
        progress = MachineRecipeHelper.clampProgress(progress, maxProgress);
        double energyPerTick = getRecipeEnergyPerTick(recipe, ENERGY_PER_TICK);

        if (!canOutput(recipe, output)) {
            return;
        }

        if (!consumeEnergy(energyPerTick)) {
            return;
        }

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        ItemStack outputNow = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (!canOutput(recipe, outputNow)) {
            progress = maxProgress;
            setChanged();
            return;
        }

        shrinkProcessInput(SLOT_INPUT, getItemHandler().getStackInSlot(SLOT_INPUT), 1);
        ItemStack result = recipe.getOutput().copy();
        mergeProcessOutput(SLOT_OUTPUT, outputNow, result);

        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private Optional<MaceratorRecipe> resolveActiveRecipe(final ItemStack input) {
        final Optional<MaceratorRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level,
                RecipeTypeRegistry.MACERATOR.get(),
                MaceratorRecipe.class,
                input,
                activeRecipeId,
                MaceratorRecipe::getInput);
        activeRecipeId = resolved.map(MaceratorRecipe::getId).orElse(null);
        return resolved;
    }

    /** Read-only recipe lookup for debug/status — does not mutate {@link #activeRecipeId}. */
    private Optional<MaceratorRecipe> peekActiveRecipe(final ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }
        return MachineRecipeHelper.resolveSingleInputRecipe(
                level,
                RecipeTypeRegistry.MACERATOR.get(),
                MaceratorRecipe.class,
                input,
                activeRecipeId,
                MaceratorRecipe::getInput);
    }

    private boolean canOutput(final MaceratorRecipe recipe, final ItemStack output) {
        ItemStack recipeOutput = recipe.getOutput();
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, recipeOutput)
                && output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize();
    }

    public boolean tryInsertInput(final ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return false;
        }
        if (!MachineRecipeHelper.acceptsSingleInput(
                level,
                RecipeTypeRegistry.MACERATOR.get(),
                MaceratorRecipe.class,
                stack,
                MaceratorRecipe::getInput)) {
            return false;
        }

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) {
            getItemHandler().setStackInSlot(SLOT_INPUT, stack.copy());
            stack.setCount(0);
            setChanged();
            return true;
        }

        if (ItemStack.isSameItemSameTags(input, stack)) {
            int transferable = Math.min(stack.getCount(), input.getMaxStackSize() - input.getCount());
            if (transferable <= 0) {
                return false;
            }
            input.grow(transferable);
            getItemHandler().setStackInSlot(SLOT_INPUT, input);
            stack.shrink(transferable);
            setChanged();
            return true;
        }

        return false;
    }

    public Component getStatusMessage() {
        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        Optional<MaceratorRecipe> recipe = peekActiveRecipe(input);
        int requiredTicks = recipe.map(MaceratorRecipe::getProcessingTime)
                .filter(time -> time > 0)
                .map(this::getScaledProcessTime)
                .orElse(getScaledProcessTime(DEFAULT_PROCESSING_TIME));

        return Component.literal(String.format(
                "Macerator | EU: %.1f / %.1f | Progress: %d / %d | Input: %s",
                getStoredEnergy(),
                getCapacity(),
                progress,
                requiredTicks,
                input.isEmpty() ? "empty" : input.getHoverName().getString()));
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe"))
                : null;
    }

    public int getProgress() {
        return progress;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.macerator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MaceratorMenu(containerId, playerInventory, this, data);
    }
}
