package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.MetalFormerMenu;
import dev.ic2port.recipe.MetalFormerRecipe;
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
 * MV metal former — faster plate and wire forming than the LV compressor.
 */
public class MetalFormerBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 8000.0D;
    public static final int TIER = EnergyTier.MV;
    public static final double ENERGY_PER_TICK = 4.0D;
    public static final int DEFAULT_PROCESSING_TIME = 100;

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

    public MetalFormerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.METAL_FORMER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return MachineRecipeHelper.acceptsSingleInput(
                level,
                RecipeTypeRegistry.METAL_FORMER.get(),
                MetalFormerRecipe.class,
                stack,
                MetalFormerRecipe::getInput);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MetalFormerBlockEntity former) {
        former.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            progress = 0;
        }

        ItemStack input = getItemHandler().getStackInSlot(SLOT_INPUT);
        ItemStack output = getItemHandler().getStackInSlot(SLOT_OUTPUT);

        ResourceLocation previousRecipeId = activeRecipeId;
        Optional<MetalFormerRecipe> recipeOptional = resolveActiveRecipe(input);
        if (recipeOptional.isEmpty()) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            activeRecipeId = null;
            setChanged();
            return;
        }

        MetalFormerRecipe recipe = recipeOptional.get();
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

        input.shrink(1);
        ItemStack result = recipe.getOutput().copy();
        if (output.isEmpty()) {
            getItemHandler().setStackInSlot(SLOT_OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }

        progress = 0;
        activeRecipeId = null;
        setChanged();
    }

    private Optional<MetalFormerRecipe> resolveActiveRecipe(final ItemStack input) {
        final Optional<MetalFormerRecipe> resolved = MachineRecipeHelper.resolveSingleInputRecipe(
                level,
                RecipeTypeRegistry.METAL_FORMER.get(),
                MetalFormerRecipe.class,
                input,
                activeRecipeId,
                MetalFormerRecipe::getInput);
        activeRecipeId = resolved.map(MetalFormerRecipe::getId).orElse(null);
        return resolved;
    }

    private boolean canOutput(final MetalFormerRecipe recipe, final ItemStack output) {
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
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
        activeRecipeId = tag.contains("ActiveRecipe")
                ? ResourceLocation.tryParse(tag.getString("ActiveRecipe"))
                : null;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.metal_former");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MetalFormerMenu(containerId, playerInventory, this, data);
    }
}
