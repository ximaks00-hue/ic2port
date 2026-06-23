package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.CapacitorCellItem;
import dev.ic2port.menu.InductionMatrixMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.BlockEntitySpillHelper;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.EnergyTransferHelper;
import dev.ic2port.util.InductionMatrixMultiblock;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Induction matrix controller — stores EU when the 5×5×5 shell is valid; capacitor cells expand capacity.
 */
public class InductionMatrixBlockEntity extends BlockEntity implements IEnergyAcceptor, IEnergyEmitter, MenuProvider {

    public static final int CELL_SLOTS = 9;
    public static final double BASE_CAPACITY = 1_000_000.0D;
    public static final double CAPACITY_PER_CASING = 250_000.0D;
    public static final double MAX_OUTPUT = EnergyTier.EV_MAX_PACKET;
    public static final int TIER = EnergyTier.EV;

    private static final int DATA_ENERGY = 0;
    private static final int DATA_CAPACITY = 1;
    private static final int DATA_STRUCTURE = 2;

    private final ItemStackHandler cellHandler = new ItemStackHandler(CELL_SLOTS) {
        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return stack.is(ItemRegistry.CAPACITOR_CELL.get());
        }

        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> cellHandlerOptional = LazyOptional.of(() -> cellHandler);

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case DATA_ENERGY -> (int) Math.min(Math.round(storedEnergy), Integer.MAX_VALUE);
                case DATA_CAPACITY -> (int) Math.min(Math.round(getEnergyCapacity()), Integer.MAX_VALUE);
                case DATA_STRUCTURE -> structureValid ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private double storedEnergy;
    private boolean structureValid;

    public InductionMatrixBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.INDUCTION_MATRIX_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final InductionMatrixBlockEntity matrix) {
        matrix.tickServer();
    }

    public IItemHandler getCellItemHandler() {
        return cellHandler;
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        structureValid = InductionMatrixMultiblock.isValid(level, worldPosition);
        if (storedEnergy > getEnergyCapacity()) {
            storedEnergy = getEnergyCapacity();
            setChanged();
        }
        if (storedEnergy > 0.0D && structureValid) {
            emitEnergy();
        }
    }

    private void emitEnergy() {
        for (Direction direction : Direction.values()) {
            double offered = Math.min(storedEnergy, MAX_OUTPUT);
            double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, direction, offered, TIER);
            double transferred = offered - remainder;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                setChanged();
                return;
            }
        }
    }

    private int countCapacitorCells() {
        int count = 0;
        for (int slot = 0; slot < CELL_SLOTS; slot++) {
            if (cellHandler.getStackInSlot(slot).is(ItemRegistry.CAPACITOR_CELL.get())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || !structureValid) {
            return amount;
        }
        if (tier > TIER) {
            return amount;
        }
        double space = getEnergyCapacity() - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    public double getEnergyCapacity() {
        double capacity = BASE_CAPACITY + countCapacitorCells() * CapacitorCellItem.CAPACITY_EU;
        if (structureValid && level != null) {
            int casings = InductionMatrixMultiblock.countCasingBlocks(level, worldPosition);
            capacity += casings * CAPACITY_PER_CASING;
        }
        return capacity;
    }

    @Override
    public double getCapacity() {
        return getEnergyCapacity();
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
        return structureValid ? Math.min(storedEnergy, MAX_OUTPUT) : 0.0D;
    }

    @Override
    public void drawEnergy(final double amount) {
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.induction_matrix");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new InductionMatrixMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putBoolean("StructureValid", structureValid);
        tag.put("Cells", cellHandler.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = tag.getDouble("StoredEnergy");
        structureValid = tag.getBoolean("StructureValid");
        if (tag.contains("Cells")) {
            cellHandler.deserializeNBT(tag.getCompound("Cells"));
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        cellHandlerOptional.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            return cellHandlerOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            BlockEntitySpillHelper.spillItems(level, worldPosition, cellHandler);
        }
        super.setRemoved();
    }
}
