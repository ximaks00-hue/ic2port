package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.OreWasherMenu;
import dev.ic2port.recipe.OreWasherRecipe;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.RecipeTypeRegistry;
import dev.ic2port.util.InsertOnlyFluidHandler;
import dev.ic2port.util.MachineRecipeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * MV ore washer — purifies crushed ore using water cells, improving ore yield.
 */
public class OreWasherBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 8_000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 4.0D;
    public static final int DEFAULT_PROCESSING_TIME = 300;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int WATER_CAPACITY_MB = 8_000;
    public static final int WATER_MB_PER_WASH = 1_000;

    private final FluidTank waterTank = new FluidTank(WATER_CAPACITY_MB, fluidStack ->
            fluidStack.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final LazyOptional<IFluidHandler> waterTankOptional =
            LazyOptional.of(() -> new InsertOnlyFluidHandler(waterTank));

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

    public OreWasherBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ORE_WASHER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override public int getTier() { return TIER; }

    @Override
    protected boolean isProcessSlotLocked(final int processSlot) {
        return progress > 0 && processSlot == SLOT_INPUT;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level, RecipeTypeRegistry.ORE_WASHER.get(),
                OreWasherRecipe.class, stack, OreWasherRecipe::getInput);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final OreWasherBlockEntity entity) {
        entity.tickServer();
    }

    private void tickServer() {
        if (!isServerProcessingEnabled()) return;
        if (consumeOverclockerLayoutReset()) progress = 0;

        pullWaterFromNeighbors();

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<OreWasherRecipe> recipeOpt = resolveActiveRecipe(input);
        if (recipeOpt.isEmpty()) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            setChanged();
            return;
        }

        OreWasherRecipe recipe = recipeOpt.get();
        if (MachineRecipeHelper.shouldResetProgress(previousRecipeId, activeRecipeId, progress)) {
            progress = 0;
        }
        maxProgress = getScaledProcessTime(
                recipe.getProcessingTime() > 0 ? recipe.getProcessingTime() : DEFAULT_PROCESSING_TIME);
        progress = MachineRecipeHelper.clampProgress(progress, maxProgress);

        if (!canOutput(recipe)) {
            return;
        }
        if (waterTank.getFluidAmount() < WATER_MB_PER_WASH) {
            return;
        }
        if (!consumeEnergy(getRecipeEnergyPerTick(recipe, ENERGY_PER_TICK))) return;

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        if (!canOutput(recipe) || waterTank.getFluidAmount() < WATER_MB_PER_WASH) {
            progress = maxProgress;
            setChanged();
            return;
        }

        shrinkProcessInput(SLOT_INPUT, getItemHandler().getStackInSlot(SLOT_INPUT), 1);
        waterTank.drain(WATER_MB_PER_WASH, IFluidHandler.FluidAction.EXECUTE);
        ItemStack result = recipe.getOutput().copy();
        ItemStack outputNow = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        mergeProcessOutput(SLOT_OUTPUT, outputNow, result);
        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private void pullWaterFromNeighbors() {
        if (level == null || waterTank.getFluidAmount() >= waterTank.getCapacity()) {
            return;
        }

        int needed = Math.min(WATER_MB_PER_WASH, waterTank.getCapacity() - waterTank.getFluidAmount());
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor == null) {
                continue;
            }
            IFluidHandler handler = neighbor.getCapability(
                    ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).orElse(null);
            if (handler == null) {
                continue;
            }
            FluidStack drained = handler.drain(needed, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty() || !drained.getFluid().isSame(Fluids.WATER)) {
                continue;
            }
            FluidStack extracted = handler.drain(needed, IFluidHandler.FluidAction.EXECUTE);
            if (extracted.isEmpty()) {
                continue;
            }
            int filled = waterTank.fill(extracted, IFluidHandler.FluidAction.EXECUTE);
            if (filled < extracted.getAmount()) {
                handler.fill(new FluidStack(Fluids.WATER, extracted.getAmount() - filled),
                        IFluidHandler.FluidAction.EXECUTE);
            }
            if (waterTank.getFluidAmount() >= waterTank.getCapacity()) {
                break;
            }
            needed = Math.min(WATER_MB_PER_WASH, waterTank.getCapacity() - waterTank.getFluidAmount());
        }
    }

    private boolean canOutput(final OreWasherRecipe recipe) {
        ItemStack out = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(out, recipe.getOutput())
                && out.getCount() + recipe.getOutput().getCount() <= out.getMaxStackSize();
    }

    private Optional<OreWasherRecipe> resolveActiveRecipe(final ItemStack input) {
        Optional<OreWasherRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level, RecipeTypeRegistry.ORE_WASHER.get(),
                OreWasherRecipe.class, input, activeRecipeId, OreWasherRecipe::getInput);
        activeRecipeId = resolved.map(OreWasherRecipe::getId).orElse(null);
        return resolved;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.put("WaterTank", waterTank.writeToNBT(new CompoundTag()));
        if (activeRecipeId != null) tag.putString("ActiveRecipe", activeRecipeId.toString());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
        if (tag.contains("WaterTank")) {
            waterTank.readFromNBT(tag.getCompound("WaterTank"));
        }
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe")) : null;
    }

    public int getProgress() { return progress; }
    public ContainerData getContainerData() { return data; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.ore_washer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory inv, final Player player) {
        return new OreWasherMenu(id, inv, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return waterTankOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        waterTankOptional.invalidate();
    }
}
