package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.InductionFurnaceMenu;
import dev.ic2port.recipe.ElectricFurnaceRecipe;
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
 * MV induction furnace — two independent lanes, 2× processing speed vs electric furnace.
 */
public class InductionFurnaceBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 16_000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 6.0D;
    public static final int DEFAULT_PROCESSING_TIME = 65;
    public static final int SPEED_DIVISOR = 2;

    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_OUTPUT_A = 1;
    public static final int SLOT_INPUT_B = 2;
    public static final int SLOT_OUTPUT_B = 3;
    public static final int SLOT_COUNT = 4;

    private int progressA;
    private int maxProgressA = DEFAULT_PROCESSING_TIME;
    private int progressB;
    private int maxProgressB = DEFAULT_PROCESSING_TIME;
    @Nullable
    private ResourceLocation activeRecipeIdA;
    @Nullable
    private ResourceLocation activeRecipeIdB;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> progressA;
                case 1 -> maxProgressA;
                case 2 -> progressB;
                case 3 -> maxProgressB;
                case 4 -> (int) Math.round(getStoredEnergy());
                case 5 -> (int) Math.round(getCapacity());
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            if (index == 0) {
                progressA = value;
            } else if (index == 2) {
                progressB = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public InductionFurnaceBlockEntity(final BlockPos pos, final BlockState state) {
        this(BlockEntityRegistry.INDUCTION_FURNACE_BE.get(), pos, state);
    }

    protected InductionFurnaceBlockEntity(
            final net.minecraft.world.level.block.entity.BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state) {
        super(type, pos, state, SLOT_COUNT, getEnergyCapacityFor(type));
    }

    protected static double getEnergyCapacityFor(final net.minecraft.world.level.block.entity.BlockEntityType<?> type) {
        return type == BlockEntityRegistry.BLAST_INDUCTION_FURNACE_BE.get() ? 24_000.0D : ENERGY_CAPACITY;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    protected int getSpeedDivisor() {
        return SPEED_DIVISOR;
    }

    protected double getFallbackEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    protected int getFallbackProcessingTime() {
        return DEFAULT_PROCESSING_TIME;
    }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_INPUT_A || processSlot == SLOT_INPUT_B;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level,
                RecipeTypeRegistry.ELECTRIC_FURNACE.get(),
                ElectricFurnaceRecipe.class,
                stack,
                ElectricFurnaceRecipe::getInput);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final InductionFurnaceBlockEntity furnace) {
        furnace.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            progressA = 0;
            progressB = 0;
        }

        processLane(SLOT_INPUT_A, SLOT_OUTPUT_A, true);
        processLane(SLOT_INPUT_B, SLOT_OUTPUT_B, false);
    }

    private void processLane(final int inputSlot, final int outputSlot, final boolean laneA) {
        ItemStack input = getItemHandler().getStackInSlot(inputSlot);
        ItemStack output = getItemHandler().getStackInSlot(outputSlot);

        ResourceLocation previousRecipeId = laneA ? activeRecipeIdA : activeRecipeIdB;
        Optional<ElectricFurnaceRecipe> recipeOptional = resolveActiveRecipe(input, laneA);
        if (recipeOptional.isEmpty()) {
            if (laneA) {
                progressA = 0;
                maxProgressA = getFallbackProcessingTime();
                activeRecipeIdA = null;
            } else {
                progressB = 0;
                maxProgressB = getFallbackProcessingTime();
                activeRecipeIdB = null;
            }
            setChanged();
            return;
        }

        ElectricFurnaceRecipe recipe = recipeOptional.get();
        ResourceLocation currentRecipeId = laneA ? activeRecipeIdA : activeRecipeIdB;
        int laneProgress = laneA ? progressA : progressB;
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, currentRecipeId, laneProgress)) {
            if (laneA) {
                progressA = 0;
            } else {
                progressB = 0;
            }
        }
        int baseTime = Math.max(
                1,
                (recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : getFallbackProcessingTime() * getSpeedDivisor())
                        / getSpeedDivisor());
        int scaledMax = getScaledProcessTime(baseTime);
        if (laneA) {
            maxProgressA = scaledMax;
            progressA = MachineRecipeHelper.clampProgress(progressA, maxProgressA);
        } else {
            maxProgressB = scaledMax;
            progressB = MachineRecipeHelper.clampProgress(progressB, maxProgressB);
        }

        if (!canOutput(recipe, output)) {
            return;
        }

        final double energyPerTick = recipe.getProcessingTime() > 0 && recipe.getEnergyCost() > 0.0D
                ? getScaledEnergyPerTick(recipe.getEnergyCost() / (double) baseTime)
                : getScaledEnergyPerTick(getFallbackEnergyPerTick());
        if (!consumeEnergy(energyPerTick)) {
            return;
        }

        if (laneA) {
            progressA++;
            if (progressA < maxProgressA) {
                setChanged();
                return;
            }
        } else {
            progressB++;
            if (progressB < maxProgressB) {
                setChanged();
                return;
            }
        }

        input.shrink(1);
        ItemStack result = recipe.getOutput().copy();
        if (output.isEmpty()) {
            getItemHandler().setStackInSlot(outputSlot, result);
        } else {
            output.grow(result.getCount());
        }

        if (laneA) {
            progressA = 0;
            activeRecipeIdA = null;
        } else {
            progressB = 0;
            activeRecipeIdB = null;
        }
        setChanged();
    }

    private Optional<ElectricFurnaceRecipe> resolveActiveRecipe(final ItemStack input, final boolean laneA) {
        final ResourceLocation cachedId = laneA ? activeRecipeIdA : activeRecipeIdB;
        final Optional<ElectricFurnaceRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level,
                RecipeTypeRegistry.ELECTRIC_FURNACE.get(),
                ElectricFurnaceRecipe.class,
                input,
                cachedId,
                ElectricFurnaceRecipe::getInput);
        final ResourceLocation resolvedId = resolved.map(ElectricFurnaceRecipe::getId).orElse(null);
        if (laneA) {
            activeRecipeIdA = resolvedId;
        } else {
            activeRecipeIdB = resolvedId;
        }
        return resolved;
    }

    private boolean canOutput(final ElectricFurnaceRecipe recipe, final ItemStack output) {
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
        tag.putInt("ProgressA", progressA);
        tag.putInt("MaxProgressA", maxProgressA);
        tag.putInt("ProgressB", progressB);
        tag.putInt("MaxProgressB", maxProgressB);
        if (activeRecipeIdA != null) {
            tag.putString("ActiveRecipeA", activeRecipeIdA.toString());
        }
        if (activeRecipeIdB != null) {
            tag.putString("ActiveRecipeB", activeRecipeIdB.toString());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progressA = tag.getInt("ProgressA");
        maxProgressA = tag.contains("MaxProgressA") ? tag.getInt("MaxProgressA") : getFallbackProcessingTime();
        progressB = tag.getInt("ProgressB");
        maxProgressB = tag.contains("MaxProgressB") ? tag.getInt("MaxProgressB") : getFallbackProcessingTime();
        activeRecipeIdA = tag.contains("ActiveRecipeA")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipeA"))
                : null;
        activeRecipeIdB = tag.contains("ActiveRecipeB")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipeB"))
                : null;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.induction_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new InductionFurnaceMenu(containerId, playerInventory, this, data);
    }
}
