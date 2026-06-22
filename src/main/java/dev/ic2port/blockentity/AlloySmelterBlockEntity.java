package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.AlloySmelterMenu;
import dev.ic2port.recipe.AlloySmelterRecipe;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.RecipeTypeRegistry;
import dev.ic2port.util.MachineRecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
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
 * MV alloy smelter — combines two inputs into one alloy output (bronze, steel, etc.).
 */
public class AlloySmelterBlockEntity extends BaseMachineBlockEntity implements Container {

    public static final double ENERGY_CAPACITY = 10_000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 5.0D;
    public static final int DEFAULT_PROCESSING_TIME = 400;

    public static final int SLOT_INPUT_A = 0;
    public static final int SLOT_INPUT_B = 1;
    public static final int SLOT_OUTPUT = 2;
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

    public AlloySmelterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ALLOY_SMELTER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override public int getTier() { return TIER; }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_INPUT_A || processSlot == SLOT_INPUT_B;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return true;
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final AlloySmelterBlockEntity entity) {
        entity.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) return;
        if (consumeOverclockerLayoutReset()) progress = 0;

        ItemStack inputA = getItemHandler().getStackInSlot(SLOT_INPUT_A);
        ItemStack inputB = getItemHandler().getStackInSlot(SLOT_INPUT_B);

        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<AlloySmelterRecipe> recipeOpt = resolveActiveRecipe(inputA, inputB);
        if (recipeOpt.isEmpty()) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            setChanged();
            return;
        }

        AlloySmelterRecipe recipe = recipeOpt.get();
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

        inputA.shrink(1);
        inputB.shrink(1);
        ItemStack result = recipe.getOutput().copy();
        ItemStack existing = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            getItemHandler().setStackInSlot(SLOT_OUTPUT, result);
        } else {
            existing.grow(result.getCount());
        }
        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private boolean canOutput(final AlloySmelterRecipe recipe) {
        ItemStack out = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(out, recipe.getOutput())
                && out.getCount() + recipe.getOutput().getCount() <= out.getMaxStackSize();
    }

    private Optional<AlloySmelterRecipe> resolveActiveRecipe(final ItemStack inputA, final ItemStack inputB) {
        if (inputA.isEmpty() || inputB.isEmpty()) {
            activeRecipeId = null;
            return Optional.empty();
        }
        Optional<AlloySmelterRecipe> resolved = level.getRecipeManager()
                .getRecipeFor(RecipeTypeRegistry.ALLOY_SMELTER.get(), this, level);
        activeRecipeId = resolved.map(AlloySmelterRecipe::getId).orElse(null);
        return resolved;
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!getItemHandler().getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }
    @Override public ItemStack getItem(final int slot) { return getItemHandler().getStackInSlot(slot); }
    @Override public ItemStack removeItem(final int slot, final int amount) {
        return getItemHandler().extractItem(slot, amount, false);
    }
    @Override public ItemStack removeItemNoUpdate(final int slot) {
        ItemStack stack = getItemHandler().getStackInSlot(slot).copy();
        getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }
    @Override public void setItem(final int slot, final ItemStack stack) {
        getItemHandler().setStackInSlot(slot, stack);
    }
    @Override public boolean stillValid(final Player player) { return true; }
    @Override public void clearContent() {
        for (int i = 0; i < SLOT_COUNT; i++) getItemHandler().setStackInSlot(i, ItemStack.EMPTY);
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
        return Component.translatable("block.ic2port.alloy_smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory inv, final Player player) {
        return new AlloySmelterMenu(id, inv, this, data);
    }
}
