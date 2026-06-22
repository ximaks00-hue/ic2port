package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorComponent;
import dev.ic2port.api.reactor.IReactorMonitor;
import dev.ic2port.block.NuclearReactorBlock;
import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.api.reactor.IReactorFuel;
import dev.ic2port.menu.NuclearReactorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyTransferHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.ProcessOnlyItemHandler;
import dev.ic2port.util.ReactorGridHelper;
import dev.ic2port.util.ReactorItemFilters;
import dev.ic2port.util.BlockEntitySpillHelper;
import dev.ic2port.util.ReactorMeltdownHelper;
import dev.ic2port.util.ReactorTickProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NuclearReactorBlockEntity extends BlockEntity
        implements IReactor, IReactorMonitor, IEnergyEmitter, MenuProvider, FullInventoryAccess {

    public static final int GRID_WIDTH = 9;
    public static final int GRID_HEIGHT = 6;
    public static final int SLOT_COUNT = GRID_WIDTH * GRID_HEIGHT;

    public static final double ENERGY_CAPACITY = 100_000.0D;
    public static final double MAX_OUTPUT_PER_TICK = EnergyTier.HV_MAX_PACKET;
    public static final int TIER = EnergyTier.HV;

    private static final int DATA_HEAT = 0;
    private static final int DATA_MAX_HEAT = 1;
    private static final int DATA_STORED_ENERGY = 2;
    private static final int DATA_MAX_ENERGY = 3;
    private static final int DATA_CHAMBER_COUNT = 4;
    private static final int DATA_ACTIVE = 5;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (!ReactorItemFilters.isAllowedInReactor(stack)) {
                return false;
            }
            return NuclearReactorBlockEntity.this.isColumnEnabled(slotToX(slot));
        }

        @Override
        public ItemStack insertItem(final int slot, final ItemStack stack, final boolean simulate) {
            if (NuclearReactorBlockEntity.this.isActive()) {
                return stack;
            }
            if (!NuclearReactorBlockEntity.this.isColumnEnabled(slotToX(slot))) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            if (NuclearReactorBlockEntity.this.isActive()) {
                return ItemStack.EMPTY;
            }
            if (!NuclearReactorBlockEntity.this.isColumnEnabled(slotToX(slot))) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final ProcessOnlyItemHandler automationItemHandler = new ProcessOnlyItemHandler(
            itemHandler, SLOT_COUNT, slot -> !isActive() && isColumnEnabled(slotToX(slot)));
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> automationItemHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case DATA_HEAT -> (int) Math.min(heat, Integer.MAX_VALUE);
                case DATA_MAX_HEAT -> (int) Math.min(getMaxHeat(), Integer.MAX_VALUE);
                case DATA_STORED_ENERGY -> (int) Math.min(storedEnergy + pendingEnergy, Integer.MAX_VALUE);
                case DATA_MAX_ENERGY -> (int) Math.min(ENERGY_CAPACITY, Integer.MAX_VALUE);
                case DATA_CHAMBER_COUNT -> chamberCount;
                case DATA_ACTIVE -> isActive() ? 1 : 0;
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

    private double heat;
    private double storedEnergy;
    private double pendingEnergy;
    private int chamberCount;
    private boolean destroyedByMeltdown;
    private int lastComparatorOutput = -1;

    public NuclearReactorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.NUCLEAR_REACTOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final NuclearReactorBlockEntity reactor) {
        reactor.tickServer();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            refreshChamberCount();
            updateComparatorOutput();
        }
    }

    public void refreshChamberCount() {
        if (level == null || level.isClientSide) {
            return;
        }
        int nextCount = ReactorGridHelper.countAdjacentChambers(
                level,
                worldPosition,
                BlockRegistry.REACTOR_CHAMBER.get());
        if (nextCount != chamberCount) {
            if (nextCount < chamberCount) {
                purgeDisabledColumns(nextCount);
            }
            chamberCount = nextCount;
            setChanged();
        }
    }

    private void purgeDisabledColumns(final int newChamberCount) {
        if (level == null) {
            return;
        }
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (ReactorGridHelper.isColumnEnabled(x, newChamberCount)) {
                    continue;
                }
                ItemStack stack = getStack(x, y);
                if (!stack.isEmpty()) {
                    Block.popResource(level, worldPosition, stack);
                    setStack(x, y, ItemStack.EMPTY);
                }
            }
        }
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByMeltdown) {
            return;
        }

        refreshChamberCount();

        if (isActive()) {
            ReactorTickProfiler.profile("fission", this::processReactorTick);
            ReactorMeltdownHelper.applyOverheatEffects(level, worldPosition, heat, getMaxHeat());
        }

        if (heat > getMaxHeat()) {
            storedEnergy = Math.min(ENERGY_CAPACITY, storedEnergy + pendingEnergy);
            pendingEnergy = 0.0D;
            meltdown();
            return;
        }

        if (pendingEnergy > 0.0D) {
            double space = ENERGY_CAPACITY - storedEnergy;
            double absorbed = Math.min(pendingEnergy, Math.max(0.0D, space));
            storedEnergy += absorbed;
            pendingEnergy -= absorbed;
        }

        if (storedEnergy > 0.0D) {
            emitEnergy();
        }

        updateComparatorOutput();
    }

    private void updateComparatorOutput() {
        if (level == null || level.isClientSide) {
            return;
        }
        int output = getHeatComparatorOutput();
        if (output != lastComparatorOutput) {
            lastComparatorOutput = output;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public boolean isActive() {
        return !destroyedByMeltdown && level != null && level.hasNeighborSignal(worldPosition);
    }

    @Override
    public double getProducedEnergy() {
        return storedEnergy + pendingEnergy;
    }

    @Override
    public boolean isEjected() {
        return destroyedByMeltdown;
    }

    @Override
    public int getOutputTier() {
        return TIER;
    }

    private void processReactorTick() {
        processComponentPhase(IReactorFuel.class);
        processComponentPhase(IReactorHeatStorage.class);
        processMiscComponents();
    }

    /** Components that are not fuel rods or heat-storage parts (e.g. neutron reflectors). */
    private void processMiscComponents() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (!isColumnEnabled(x)) {
                    continue;
                }
                ItemStack stack = getStack(x, y);
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.getItem() instanceof IReactorFuel || stack.getItem() instanceof IReactorHeatStorage) {
                    continue;
                }
                if (stack.getItem() instanceof IReactorComponent component) {
                    component.processTick(this, stack, x, y);
                }
            }
        }
    }

    private void processComponentPhase(final Class<? extends IReactorComponent> componentType) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (!isColumnEnabled(x)) {
                    continue;
                }
                ItemStack stack = getStack(x, y);
                if (stack.isEmpty() || !componentType.isInstance(stack.getItem())) {
                    continue;
                }
                ((IReactorComponent) stack.getItem()).processTick(this, stack, x, y);
            }
        }
    }

    private void emitEnergy() {
        Direction outputDirection = getBlockState().getValue(NuclearReactorBlock.FACING);
        double offered = Math.min(storedEnergy, MAX_OUTPUT_PER_TICK);
        double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, outputDirection, offered, TIER);
        double transferred = offered - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    private void meltdown() {
        if (level == null || level.isClientSide || destroyedByMeltdown) {
            return;
        }
        destroyedByMeltdown = true;

        float explosionPower = ReactorMeltdownHelper.explosionPowerForInventory(this);
        int contaminationRadius = ReactorMeltdownHelper.contaminationRadiusForInventory(this);

        BlockEntitySpillHelper.spillItems(level, worldPosition, itemHandler);

        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;
        ReactorMeltdownHelper.contaminateArea(level, worldPosition, contaminationRadius);
        level.removeBlock(worldPosition, false);
        level.explode(null, centerX, centerY, centerZ, explosionPower, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public int getGridWidth() {
        return GRID_WIDTH;
    }

    @Override
    public int getGridHeight() {
        return GRID_HEIGHT;
    }

    @Override
    public boolean isInBounds(final int x, final int y) {
        return x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT;
    }

    @Override
    public ItemStack getStack(final int x, final int y) {
        if (!isInBounds(x, y)) {
            return ItemStack.EMPTY;
        }
        return itemHandler.getStackInSlot(toSlotIndex(x, y));
    }

    @Override
    public void setStack(final int x, final int y, final ItemStack stack) {
        if (!isInBounds(x, y)) {
            return;
        }
        itemHandler.setStackInSlot(toSlotIndex(x, y), stack);
        setChanged();
    }

    @Override
    public double getHeat() {
        return heat;
    }

    @Override
    public void addHeat(final double amount) {
        if (amount <= 0.0D) {
            return;
        }
        heat += amount;
        setChanged();
        updateComparatorOutput();
    }

    @Override
    public double removeHeat(final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double removed = Math.min(amount, heat);
        heat -= removed;
        setChanged();
        updateComparatorOutput();
        return removed;
    }

    @Override
    public double getMaxHeat() {
        double bonus = 0.0D;
        for (int x = 0; x < getGridWidth(); x++) {
            for (int y = 0; y < getGridHeight(); y++) {
                net.minecraft.world.item.ItemStack stack = getStack(x, y);
                if (stack.getItem() instanceof dev.ic2port.item.ReactorPlatingItem) {
                    bonus += dev.ic2port.item.ReactorPlatingItem.CAPACITY_BONUS;
                }
            }
        }
        return ModConfig.REACTOR_MAX_HEAT.get() + bonus;
    }

    @Override
    public void addGeneratedEnergy(final double amount) {
        if (amount <= 0.0D) {
            return;
        }
        final double space = ENERGY_CAPACITY - (storedEnergy + pendingEnergy);
        if (space <= 0.0D) {
            return;
        }
        pendingEnergy += Math.min(amount, space);
    }

    @Override
    public int getChamberCount() {
        return chamberCount;
    }

    @Override
    public boolean isColumnEnabled(final int x) {
        return ReactorGridHelper.isColumnEnabled(x, chamberCount);
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public BlockPos getPosition() {
        return worldPosition;
    }

    public static int toSlotIndex(final int x, final int y) {
        return x + y * GRID_WIDTH;
    }

    public static int slotToX(final int slot) {
        return slot % GRID_WIDTH;
    }

    public static int slotToY(final int slot) {
        return slot / GRID_WIDTH;
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
        return Math.min(storedEnergy, MAX_OUTPUT_PER_TICK);
    }

    @Override
    public void drawEnergy(final double amount) {
        if (amount <= 0.0D) {
            return;
        }
        storedEnergy = Math.max(0.0D, storedEnergy - amount);
        setChanged();
    }

    @Override
    public IItemHandler getFullItemHandler() {
        return itemHandler;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putDouble("Heat", heat);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putDouble("PendingEnergy", pendingEnergy);
        tag.putInt("ChamberCount", chamberCount);
        tag.putBoolean("DestroyedByMeltdown", destroyedByMeltdown);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        heat = Math.min(tag.getDouble("Heat"), getMaxHeat());
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        pendingEnergy = Math.min(tag.getDouble("PendingEnergy"), Math.max(0.0D, ENERGY_CAPACITY - storedEnergy));
        chamberCount = tag.getInt("ChamberCount");
        destroyedByMeltdown = tag.getBoolean("DestroyedByMeltdown");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.nuclear_reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new NuclearReactorMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            if (side == null || side == getBlockState().getValue(NuclearReactorBlock.FACING)) {
                return energyOptional.cast();
            }
            return LazyOptional.empty();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
        energyOptional.invalidate();
    }
}
