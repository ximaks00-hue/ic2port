package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.ExtractorMenu;
import dev.ic2port.recipe.ExtractorRecipe;
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

public class ExtractorBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 4000.0D;
    public static final int TIER = EnergyTier.LV;
    public static final double ENERGY_PER_TICK = 2.0D;
    public static final int DEFAULT_PROCESSING_TIME = 200;

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

    public ExtractorBlockEntity(final BlockPos pos, final BlockState state) {
        this(BlockEntityRegistry.EXTRACTOR_BE.get(), pos, state);
    }

    protected ExtractorBlockEntity(
            final net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state) {
        super(type, pos, state, SLOT_COUNT, getEnergyCapacityFor(type));
    }

    protected static double getEnergyCapacityFor(final net.minecraft.world.level.block.entity.BlockEntityType<?> type) {
        return type == BlockEntityRegistry.CENTRIFUGAL_EXTRACTOR_BE.get() ? 8_000.0D : ENERGY_CAPACITY;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    protected int getProcessTimeDivisor() {
        return 1;
    }

    protected double getFallbackEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    protected int getFallbackProcessingTime() {
        return DEFAULT_PROCESSING_TIME;
    }

    @Override
    protected boolean isProcessSlotLocked(final int processSlot) {
        return progress > 0 && processSlot == SLOT_INPUT;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level,
                RecipeTypeRegistry.EXTRACTOR.get(),
                ExtractorRecipe.class,
                stack,
                ExtractorRecipe::getInput);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final ExtractorBlockEntity extractor) {
        extractor.tickServer();
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
        Optional<ExtractorRecipe> recipeOptional = resolveActiveRecipe(input);
        if (recipeOptional.isEmpty()) {
            progress = 0;
            maxProgress = getFallbackProcessingTime();
            activeRecipeId = null;
            setChanged();
            return;
        }

        ExtractorRecipe recipe = recipeOptional.get();
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, activeRecipeId, progress)) {
            progress = 0;
        }
        int baseProcessTime = Math.max(
                1,
                (recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : getFallbackProcessingTime())
                        / getProcessTimeDivisor());
        maxProgress = getScaledProcessTime(baseProcessTime);
        progress = MachineRecipeHelper.clampProgress(progress, maxProgress);
        double energyPerTick = getRecipeEnergyPerTick(recipe, getFallbackEnergyPerTick());

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

    private Optional<ExtractorRecipe> resolveActiveRecipe(final ItemStack input) {
        final Optional<ExtractorRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level,
                RecipeTypeRegistry.EXTRACTOR.get(),
                ExtractorRecipe.class,
                input,
                activeRecipeId,
                ExtractorRecipe::getInput);
        activeRecipeId = resolved.map(ExtractorRecipe::getId).orElse(null);
        return resolved;
    }

    private boolean canOutput(final ExtractorRecipe recipe, final ItemStack output) {
        ItemStack recipeOutput = recipe.getOutput();
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, recipeOutput)
                && output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize();
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
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : getFallbackProcessingTime();
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe"))
                : null;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new ExtractorMenu(containerId, playerInventory, this, data);
    }
}
