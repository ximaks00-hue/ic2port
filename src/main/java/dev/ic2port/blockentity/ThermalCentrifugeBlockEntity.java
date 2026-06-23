package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.item.CentrifugeRotorItem;
import dev.ic2port.menu.ThermalCentrifugeMenu;
import dev.ic2port.recipe.CentrifugeRecipe;
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

public class ThermalCentrifugeBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 50_000.0D;
    public static final int TIER = EnergyTier.MV;

    public static final double MIN_OPERATING_HEAT = 5_000.0D;
    public static final double MAX_ROTOR_HEAT = 10_000.0D;
    public static final double HEAT_UP_EU_PER_TICK = 64.0D;
    public static final double HEAT_UP_GAIN_PER_TICK = 100.0D;
    public static final double PROCESS_EU_PER_TICK = 48.0D;
    public static final double HEAT_LOSS_PROCESSING = 25.0D;
    public static final double HEAT_BUILDUP_PROCESSING = 35.0D;
    public static final double HEAT_LOSS_IDLE = 50.0D;

    public static final int DEFAULT_PROCESSING_TIME = 600;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_PRIMARY = 1;
    public static final int SLOT_OUTPUT_SECONDARY = 2;
    public static final int SLOT_OUTPUT_TERTIARY = 3;
    public static final int SLOT_ROTOR = 4;
    public static final int SLOT_COUNT = 5;

    public static final int DATA_ROTOR_HEAT = 4;
    public static final int DATA_MAX_ROTOR_HEAT = 5;

    private int progress;
    private int maxProgress = DEFAULT_PROCESSING_TIME;
    private double rotorHeat;
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
                case DATA_ROTOR_HEAT -> (int) Math.min(rotorHeat, Integer.MAX_VALUE);
                case DATA_MAX_ROTOR_HEAT -> (int) MAX_ROTOR_HEAT;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public ThermalCentrifugeBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.THERMAL_CENTRIFUGE_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override
    protected boolean isProcessSlotLocked(final int processSlot) {
        return progress > 0 && (processSlot == SLOT_INPUT || processSlot == SLOT_ROTOR);
    }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_INPUT || processSlot == SLOT_ROTOR;
    }

    @Override
    protected boolean canAutomationExtractFromSlot(final int processSlot) {
        return processSlot == SLOT_OUTPUT_PRIMARY
                || processSlot == SLOT_OUTPUT_SECONDARY
                || processSlot == SLOT_OUTPUT_TERTIARY;
    }

    @Override
    protected boolean isValidProcessInput(final int processSlot, final ItemStack stack) {
        if (processSlot == SLOT_ROTOR) {
            return CentrifugeRotorItem.isRotorStack(stack);
        }
        if (processSlot == SLOT_INPUT) {
            return MachineRecipeHelper.acceptsCentrifugeInput(
                    level, stack, RecipeTypeRegistry.THERMAL_CENTRIFUGE.get());
        }
        return false;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final ThermalCentrifugeBlockEntity centrifuge) {
        centrifuge.tickServer();
    }

    private void tickServer() {
        if (!isServerProcessingEnabled()) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            progress = 0;
        }

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        ItemStack rotor = getItemHandler().getStackInSlot(SLOT_ROTOR);
        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<CentrifugeRecipe> recipeOptional = resolveActiveRecipe(input);

        if (recipeOptional.isEmpty() || !CentrifugeRotorItem.isUsable(rotor)) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            decayRotorHeat(HEAT_LOSS_IDLE);
            setChanged();
            return;
        }

        CentrifugeRecipe recipe = recipeOptional.get();
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, activeRecipeId, progress)) {
            progress = 0;
        }
        int baseProcessTime = recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : DEFAULT_PROCESSING_TIME;
        maxProgress = getScaledProcessTime(baseProcessTime);
        progress = MachineRecipeHelper.clampProgress(progress, maxProgress);

        if (rotorHeat >= MAX_ROTOR_HEAT) {
            progress = 0;
            breakRotorFromOverheat();
            rotorHeat = 0.0D;
            setChanged();
            return;
        }

        if (rotorHeat < MIN_OPERATING_HEAT) {
            progress = 0;
            if (!canFitOutputs(recipe)) {
                decayRotorHeat(HEAT_LOSS_IDLE);
                setChanged();
                return;
            }
            if (rotorHeat + HEAT_UP_GAIN_PER_TICK < MAX_ROTOR_HEAT
                    && consumeEnergy(getScaledEnergyPerTick(HEAT_UP_EU_PER_TICK))) {
                rotorHeat += HEAT_UP_GAIN_PER_TICK;
                if (rotorHeat >= MAX_ROTOR_HEAT) {
                    breakRotorFromOverheat();
                    rotorHeat = 0.0D;
                }
            }
            setChanged();
            return;
        }

        if (!canFitOutputs(recipe)) {
            setChanged();
            return;
        }

        double energyPerTick = getRecipeEnergyPerTick(recipe, PROCESS_EU_PER_TICK);
        if (rotorHeat + HEAT_BUILDUP_PROCESSING - HEAT_LOSS_PROCESSING >= MAX_ROTOR_HEAT) {
            progress = 0;
            decayRotorHeat(HEAT_LOSS_IDLE);
            setChanged();
            return;
        }
        if (!consumeEnergy(energyPerTick)) {
            setChanged();
            return;
        }

        rotorHeat = rotorHeat - HEAT_LOSS_PROCESSING + HEAT_BUILDUP_PROCESSING;
        if (rotorHeat >= MAX_ROTOR_HEAT) {
            progress = 0;
            breakRotorFromOverheat();
            rotorHeat = 0.0D;
            setChanged();
            return;
        }

        if (rotorHeat < MIN_OPERATING_HEAT) {
            setChanged();
            return;
        }

        progress++;
        if (progress < maxProgress) {
            applyRotorWear(1);
            setChanged();
            return;
        }

        if (!canFitOutputs(recipe)) {
            progress = maxProgress;
            setChanged();
            return;
        }

        shrinkProcessInput(SLOT_INPUT, getItemHandler().getStackInSlot(SLOT_INPUT), recipe.getInputCount());
        depositOutputs(recipe);
        applyRotorWear(1);
        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private void applyRotorWear(final int amount) {
        ItemStack rotor = getItemHandler().getStackInSlot(SLOT_ROTOR);
        CentrifugeRotorItem.applyWear(rotor, amount);
        getItemHandler().setStackInSlot(SLOT_ROTOR, rotor);
    }

    private void decayRotorHeat(final double amount) {
        if (amount <= 0.0D || rotorHeat <= 0.0D) {
            return;
        }
        rotorHeat = Math.max(0.0D, rotorHeat - amount);
    }

    private void breakRotorFromOverheat() {
        ItemStack rotor = getItemHandler().getStackInSlot(SLOT_ROTOR);
        if (!rotor.isEmpty()) {
            getItemHandler().setStackInSlot(SLOT_ROTOR, ItemStack.EMPTY);
        }
    }

    private void depositOutputs(final CentrifugeRecipe recipe) {
        int outputSlot = SLOT_OUTPUT_PRIMARY;
        for (CentrifugeRecipe.OutputStack output : recipe.getOutputs()) {
            if (outputSlot > SLOT_OUTPUT_TERTIARY) {
                break;
            }
            ItemStack result = output.copy();
            ItemStack existing = getItemHandler().getStackInSlot(outputSlot);
            mergeProcessOutput(outputSlot, existing, result);
            outputSlot++;
        }
    }

    private boolean canFitOutputs(final CentrifugeRecipe recipe) {
        int outputSlot = SLOT_OUTPUT_PRIMARY;
        for (CentrifugeRecipe.OutputStack output : recipe.getOutputs()) {
            if (outputSlot > SLOT_OUTPUT_TERTIARY) {
                return false;
            }
            if (!canStackIntoSlot(outputSlot, output.copy())) {
                return false;
            }
            outputSlot++;
        }
        return true;
    }

    private boolean canStackIntoSlot(final int slot, final ItemStack recipeOutput) {
        ItemStack existing = getItemHandler().getStackInSlot(slot);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(existing, recipeOutput)
                && existing.getCount() + recipeOutput.getCount() <= existing.getMaxStackSize();
    }

    private Optional<CentrifugeRecipe> resolveActiveRecipe(final ItemStack input) {
        final Optional<CentrifugeRecipe> resolved = MachineRecipeHelper.resolveCentrifugeRecipe(
                level,
                input,
                activeRecipeId,
                RecipeTypeRegistry.THERMAL_CENTRIFUGE.get());
        activeRecipeId = resolved.map(CentrifugeRecipe::getId).orElse(null);
        return resolved;
    }

    public boolean isHeating() {
        return rotorHeat < MIN_OPERATING_HEAT
                && CentrifugeRotorItem.isUsable(getItemHandler().getStackInSlot(SLOT_ROTOR))
                && !getItemHandler().getStackInSlot(SLOT_INPUT).isEmpty();
    }

    public boolean isProcessing() {
        return rotorHeat >= MIN_OPERATING_HEAT && progress > 0;
    }

    public double getRotorHeat() {
        return rotorHeat;
    }

    public double getMaxRotorHeat() {
        return MAX_ROTOR_HEAT;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putDouble("RotorHeat", rotorHeat);
        if (activeRecipeId != null) {
            tag.putString("ActiveRecipe", activeRecipeId.toString());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
        rotorHeat = tag.getDouble("RotorHeat");
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe"))
                : null;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.thermal_centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new ThermalCentrifugeMenu(containerId, playerInventory, this, data);
    }
}
