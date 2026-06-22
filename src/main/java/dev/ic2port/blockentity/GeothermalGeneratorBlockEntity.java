package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.block.GeothermalGeneratorBlock;
import dev.ic2port.menu.GeothermalGeneratorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LV geothermal EU generator — burns lava from an internal tank or lava buckets.
 */
public class GeothermalGeneratorBlockEntity extends BlockEntity implements IEnergyEmitter, MenuProvider {

    public static final int FLUID_CAPACITY_MB = 8_000;
    public static final double ENERGY_CAPACITY = 2_400.0D;
    public static final double GENERATION_PER_TICK = 20.0D;
    public static final int LAVA_MB_PER_BUCKET = 1_000;
    public static final int LAVA_MB_PER_TICK = 1;
    public static final int TIER = EnergyTier.LV;

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY_MB, fluidStack ->
            fluidStack.getFluid().isSame(Fluids.LAVA)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final LazyOptional<IFluidHandler> fluidHandlerOptional = LazyOptional.of(() -> fluidTank);

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (slot == SLOT_INPUT) {
                return stack.is(Items.LAVA_BUCKET);
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            return false;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.round(storedEnergy);
                case 1 -> (int) Math.round(ENERGY_CAPACITY);
                case 2 -> fluidTank.getFluidAmount();
                case 3 -> FLUID_CAPACITY_MB;
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

    private double storedEnergy;

    public GeothermalGeneratorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.GEOTHERMAL_GENERATOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final GeothermalGeneratorBlockEntity generator) {
        generator.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        processLavaBuckets();
        generateEnergy();
        updateLitState();
        distributeEnergy();
    }

    private void processLavaBuckets() {
        ItemStack inputStack = itemHandler.getStackInSlot(SLOT_INPUT);
        if (!inputStack.is(Items.LAVA_BUCKET)) {
            return;
        }
        if (fluidTank.getFluidAmount() + LAVA_MB_PER_BUCKET > FLUID_CAPACITY_MB) {
            return;
        }

        ItemStack outputStack = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (!outputStack.isEmpty() && (!outputStack.is(Items.BUCKET) || outputStack.getCount() >= outputStack.getMaxStackSize())) {
            return;
        }

        inputStack.shrink(1);
        fluidTank.fill(new FluidStack(Fluids.LAVA, LAVA_MB_PER_BUCKET), IFluidHandler.FluidAction.EXECUTE);

        if (outputStack.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(Items.BUCKET));
        } else {
            outputStack.grow(1);
        }
        setChanged();
    }

    private void generateEnergy() {
        if (fluidTank.getFluidAmount() < LAVA_MB_PER_TICK) {
            return;
        }

        double space = ENERGY_CAPACITY - storedEnergy;
        if (space < GENERATION_PER_TICK) {
            return;
        }

        int drained = fluidTank.drain(LAVA_MB_PER_TICK, IFluidHandler.FluidAction.EXECUTE).getAmount();
        if (drained <= 0) {
            return;
        }

        storedEnergy += GENERATION_PER_TICK;
        setChanged();
    }

    private void distributeEnergy() {
        if (storedEnergy <= 0.0D || level == null) {
            return;
        }

        Direction outputDirection = getOutputDirection();
        double toSend = Math.min(storedEnergy, GENERATION_PER_TICK);
        double remainder = EnergyTransferHelper.injectIntoNeighbor(
                level, worldPosition, outputDirection, toSend, TIER);
        double transferred = toSend - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    private Direction getOutputDirection() {
        return getBlockState().getValue(GeothermalGeneratorBlock.FACING).getOpposite();
    }

    private void updateLitState() {
        if (level == null || level.isClientSide) {
            return;
        }

        boolean active = fluidTank.getFluidAmount() > 0 && storedEnergy < ENERGY_CAPACITY;
        BlockState currentState = getBlockState();
        if (currentState.getValue(GeothermalGeneratorBlock.LIT) != active) {
            level.setBlock(worldPosition, currentState.setValue(GeothermalGeneratorBlock.LIT, active), Block.UPDATE_ALL);
        }
    }

    @Override
    public double getCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    public double getOfferedEnergy() {
        return Math.min(storedEnergy, GENERATION_PER_TICK);
    }

    @Override
    public void drawEnergy(final double amount) {
        if (amount <= 0.0D) {
            return;
        }
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("FluidTank", fluidTank.writeToNBT(new CompoundTag()));
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        fluidTank.readFromNBT(tag.getCompound("FluidTank"));
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.geothermal_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new GeothermalGeneratorMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandlerOptional.cast();
        }
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
        fluidHandlerOptional.invalidate();
        energyOptional.invalidate();
    }
}
