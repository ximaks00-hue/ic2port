package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.ElectrolyzerMenu;
import dev.ic2port.recipe.ElectrolyzerRecipe;
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
 * MV electrolyzer — uses EU to decompose materials (e.g., water cells → hydrogen + oxygen cells).
 */
public class ElectrolyzerBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 10_000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 5.0D;
    public static final int DEFAULT_PROCESSING_TIME = 400;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_A = 1;
    public static final int SLOT_OUTPUT_B = 2;
    public static final int SLOT_COUNT = 3;

    private int progress;
    private int maxProgress = DEFAULT_PROCESSING_TIME;
    @Nullable private ResourceLocation activeRecipeId;

    private final ContainerData data = new ContainerData() {
        @Override public int get(final int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> (int) Math.round(getStoredEnergy());
                case 3 -> (int) Math.round(getCapacity());
                default -> 0;
            };
        }
        @Override public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }
        @Override public int getCount() { return 4; }
    };

    public ElectrolyzerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ELECTROLYZER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override public int getTier() { return TIER; }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_INPUT;
    }

    @Override
    protected boolean canAutomationExtractFromSlot(final int processSlot) {
        return processSlot == SLOT_OUTPUT_A || processSlot == SLOT_OUTPUT_B;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level, RecipeTypeRegistry.ELECTROLYZER.get(),
                ElectrolyzerRecipe.class, stack, ElectrolyzerRecipe::getInput);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final ElectrolyzerBlockEntity entity) {
        entity.tickServer();
    }

    private void tickServer() {
        if (!isServerProcessingEnabled()) return;
        if (consumeOverclockerLayoutReset()) progress = 0;

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<ElectrolyzerRecipe> recipeOpt = resolveActiveRecipe(input);
        if (recipeOpt.isEmpty()) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            setChanged();
            return;
        }

        ElectrolyzerRecipe recipe = recipeOpt.get();
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, activeRecipeId, progress)) {
            progress = 0;
        }
        maxProgress = getScaledProcessTime(
                recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : DEFAULT_PROCESSING_TIME);
        progress = MachineRecipeHelper.clampProgress(progress, maxProgress);

        if (!canOutput(recipe)) return;
        if (!consumeEnergy(getRecipeEnergyPerTick(recipe, ENERGY_PER_TICK))) return;

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        if (!canOutput(recipe)) {
            progress = maxProgress;
            setChanged();
            return;
        }

        shrinkProcessInput(SLOT_INPUT, getItemHandler().getStackInSlot(SLOT_INPUT), 1);
        ItemStack outputANow = getItemHandler().getStackInSlot(SLOT_OUTPUT_A);
        mergeProcessOutput(SLOT_OUTPUT_A, outputANow, recipe.getOutput().copy());
        if (!recipe.getSecondaryOutput().isEmpty()) {
            ItemStack outputBNow = getItemHandler().getStackInSlot(SLOT_OUTPUT_B);
            mergeProcessOutput(SLOT_OUTPUT_B, outputBNow, recipe.getSecondaryOutput().copy());
        }
        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private boolean canOutput(final ElectrolyzerRecipe recipe) {
        return canFit(SLOT_OUTPUT_A, recipe.getOutput())
                && (recipe.getSecondaryOutput().isEmpty() || canFit(SLOT_OUTPUT_B, recipe.getSecondaryOutput()));
    }

    private boolean canFit(final int slot, final ItemStack result) {
        ItemStack existing = getItemHandler().getStackInSlot(slot);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, result)
                && existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private Optional<ElectrolyzerRecipe> resolveActiveRecipe(final ItemStack input) {
        Optional<ElectrolyzerRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level, RecipeTypeRegistry.ELECTROLYZER.get(),
                ElectrolyzerRecipe.class, input, activeRecipeId, ElectrolyzerRecipe::getInput);
        activeRecipeId = resolved.map(ElectrolyzerRecipe::getId).orElse(null);
        return resolved;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        if (activeRecipeId != null) tag.putString("ActiveRecipe", activeRecipeId.toString());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe")) : null;
    }

    public int getProgress() { return progress; }
    public ContainerData getContainerData() { return data; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.electrolyzer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory inv, final Player player) {
        return new ElectrolyzerMenu(id, inv, this, data);
    }
}
