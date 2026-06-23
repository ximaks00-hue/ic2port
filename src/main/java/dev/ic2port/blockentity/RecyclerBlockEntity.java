package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.RecyclerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.RecyclerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * LV recycler — destroys junk items with a small chance to produce scrap (IC2-style).
 */
public class RecyclerBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 4000.0D;
    public static final int TIER = EnergyTier.LV;
    public static final double ENERGY_PER_TICK = 1.0D;
    public static final int DEFAULT_PROCESSING_TIME = 45;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private int progress;
    private int maxProgress = DEFAULT_PROCESSING_TIME;

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

    public RecyclerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.RECYCLER_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override
    protected boolean isValidProcessInput(final ItemStack stack) {
        return RecyclerHelper.canRecycle(stack);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    protected boolean isProcessSlotLocked(final int processSlot) {
        return progress > 0 && processSlot == SLOT_INPUT;
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final RecyclerBlockEntity recycler) {
        recycler.tickServer();
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

        if (!RecyclerHelper.canRecycle(input)) {
            progress = 0;
            maxProgress = DEFAULT_PROCESSING_TIME;
            setChanged();
            return;
        }

        maxProgress = getScaledProcessTime(DEFAULT_PROCESSING_TIME);

        if (!output.isEmpty() && !isRecyclerProduct(output)) {
            setChanged();
            return;
        }

        if (!output.isEmpty() && !canMergeRecyclerProduct(output)) {
            setChanged();
            return;
        }

        if (!consumeEnergy(getScaledEnergyPerTick(ENERGY_PER_TICK))) {
            return;
        }

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        ItemStack outputNow = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (!outputNow.isEmpty() && !isRecyclerProduct(outputNow)) {
            progress = maxProgress;
            setChanged();
            return;
        }

        shrinkProcessInput(SLOT_INPUT, getItemHandler().getStackInSlot(SLOT_INPUT), 1);

        float roll = level.random.nextFloat();
        float scrapBoxChance = ModConfig.RECYCLER_SCRAP_BOX_CHANCE.get().floatValue();
        float scrapChance = ModConfig.RECYCLER_SCRAP_CHANCE.get().floatValue();
        if (roll < scrapBoxChance) {
            emitRecyclerProduct(new ItemStack(ItemRegistry.SCRAP_BOX.get()));
        } else if (roll < scrapBoxChance + scrapChance) {
            emitRecyclerProduct(new ItemStack(ItemRegistry.SCRAP.get()));
        }

        progress = 0;
        setChanged();
    }

    private void emitRecyclerProduct(final ItemStack product) {
        ItemStack currentOutput = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (canMergeRecyclerProduct(currentOutput)) {
            mergeProcessOutput(SLOT_OUTPUT, currentOutput, product);
        } else if (level != null) {
            Block.popResource(level, worldPosition, product);
        }
    }

    private static boolean isRecyclerProduct(final ItemStack stack) {
        return stack.is(ItemRegistry.SCRAP.get()) || stack.is(ItemRegistry.SCRAP_BOX.get());
    }

    private boolean canMergeRecyclerProduct(final ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        if (!isRecyclerProduct(output)) {
            return false;
        }
        Item productItem = output.is(ItemRegistry.SCRAP_BOX.get())
                ? ItemRegistry.SCRAP_BOX.get()
                : ItemRegistry.SCRAP.get();
        return output.is(productItem) && output.getCount() < output.getMaxStackSize();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESSING_TIME;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.recycler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new RecyclerMenu(containerId, playerInventory, this, data);
    }
}
