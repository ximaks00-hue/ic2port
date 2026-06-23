package dev.ic2port.blockentity;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorMonitor;
import dev.ic2port.api.reactor.IReactorComponent;
import dev.ic2port.api.reactor.IReactorFuel;
import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.block.SteamReactorBlock;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.util.BlockEntitySpillHelper;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.Ic2Fluids;
import dev.ic2port.util.ProcessOnlyItemHandler;
import dev.ic2port.util.ReactorGridHelper;
import dev.ic2port.util.ReactorItemFilters;
import dev.ic2port.util.ReactorMeltdownHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import dev.ic2port.menu.SteamReactorMenu;
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
 * MVP steam reactor — fission heat drives steam (water) production exported to adjacent tanks.
 */
public class SteamReactorBlockEntity extends BlockEntity implements IReactor, IReactorMonitor, FullInventoryAccess, MenuProvider {

    private static final int DATA_HEAT = 0;
    private static final int DATA_MAX_HEAT = 1;
    private static final int DATA_STEAM = 2;
    private static final int DATA_STEAM_CAPACITY = 3;
    private static final int DATA_CHAMBER_COUNT = 4;
    private static final int DATA_ACTIVE = 5;

    public static final int GRID_WIDTH = 9;
    public static final int GRID_HEIGHT = 6;
    public static final int SLOT_COUNT = GRID_WIDTH * GRID_HEIGHT;
    public static final int STEAM_TANK_MB = 16_000;
    public static final int STEAM_PER_TICK_MB = 100;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (!ReactorItemFilters.isAllowedInReactor(stack)) {
                return false;
            }
            return SteamReactorBlockEntity.this.isColumnEnabled(slotToX(slot));
        }

        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final ProcessOnlyItemHandler automationItemHandler = new ProcessOnlyItemHandler(
            itemHandler, SLOT_COUNT, slot -> !isActive() && isColumnEnabled(slotToX(slot)));
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> automationItemHandler);

    private final FluidTank steamTank = new FluidTank(STEAM_TANK_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final LazyOptional<IFluidHandler> steamTankOptional = LazyOptional.of(() -> steamTank);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case DATA_HEAT -> (int) Math.min(heat, Integer.MAX_VALUE);
                case DATA_MAX_HEAT -> (int) Math.min(getMaxHeat(), Integer.MAX_VALUE);
                case DATA_STEAM -> steamTank.getFluidAmount();
                case DATA_STEAM_CAPACITY -> steamTank.getCapacity();
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
    private int chamberCount;

    public SteamReactorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.STEAM_REACTOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final SteamReactorBlockEntity reactor) {
        reactor.tickServer();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            refreshChamberCount();
        }
    }

    public void refreshChamberCount() {
        if (level == null || level.isClientSide) {
            return;
        }
        int nextCount = ReactorGridHelper.countAdjacentChambers(
                level, worldPosition, BlockRegistry.STEAM_CHAMBER.get());
        if (nextCount != chamberCount) {
            chamberCount = nextCount;
            setChanged();
        }
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        refreshChamberCount();

        if (isActive()) {
            processComponentPhase(IReactorFuel.class);
            processComponentPhase(IReactorHeatStorage.class);
            produceSteam();
        } else if (heat > 0.0D) {
            processComponentPhase(IReactorHeatStorage.class);
        }

        if (heat > 0.0D) {
            ReactorMeltdownHelper.applyOverheatEffects(level, worldPosition, heat, getMaxHeat());
        }

        if (heat > getMaxHeat()) {
            meltdown();
            return;
        }

        exportSteam();
    }

    private void produceSteam() {
        if (heat < 100.0D || steamTank.getFluidAmount() >= steamTank.getCapacity()) {
            return;
        }
        int space = steamTank.getCapacity() - steamTank.getFluidAmount();
        int toFill = Math.min(STEAM_PER_TICK_MB, space);
        steamTank.fill(Ic2Fluids.steamStack(toFill), IFluidHandler.FluidAction.EXECUTE);
        heat -= toFill * 0.5D;
        setChanged();
    }

    private void exportSteam() {
        if (level == null || steamTank.getFluidAmount() <= 0) {
            return;
        }
        Direction output = getBlockState().getValue(SteamReactorBlock.FACING);
        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(output));
        if (neighbor == null) {
            return;
        }
        IFluidHandler handler = neighbor.getCapability(
                ForgeCapabilities.FLUID_HANDLER, output.getOpposite()).orElse(null);
        if (handler == null) {
            return;
        }
        FluidStack drained = steamTank.drain(500, IFluidHandler.FluidAction.SIMULATE);
        if (drained.isEmpty()) {
            return;
        }
        FluidStack toPush = steamTank.drain(drained.getAmount(), IFluidHandler.FluidAction.EXECUTE);
        int filled = handler.fill(toPush, IFluidHandler.FluidAction.EXECUTE);
        if (filled < toPush.getAmount()) {
            steamTank.fill(new FluidStack(toPush.getFluid(), toPush.getAmount() - filled),
                    IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
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

    private void meltdown() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockEntitySpillHelper.spillItems(level, worldPosition, itemHandler);
        steamTank.setFluid(FluidStack.EMPTY);
        ReactorMeltdownHelper.contaminateArea(level, worldPosition, 4);
        level.removeBlock(worldPosition, false);
        level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
                4.0F, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public double getProducedEnergy() {
        return steamTank.getFluidAmount();
    }

    @Override
    public boolean isEjected() {
        return false;
    }

    @Override
    public int getOutputTier() {
        return 0;
    }

    @Override
    public boolean isActive() {
        return level != null && level.hasNeighborSignal(worldPosition);
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
        return itemHandler.getStackInSlot(toSlotIndex(x, y));
    }

    @Override
    public void setStack(final int x, final int y, final ItemStack stack) {
        itemHandler.setStackInSlot(toSlotIndex(x, y), stack);
    }

    @Override
    public double getHeat() {
        return heat;
    }

    @Override
    public void addHeat(final double amount) {
        heat += amount;
        setChanged();
    }

    @Override
    public double getMaxHeat() {
        return 10_000.0D + chamberCount * 1_000.0D;
    }

    @Override
    public void addGeneratedEnergy(final double amount) {
        addHeat(amount * 0.1D);
    }

    @Override
    public double removeHeat(final double amount) {
        double removed = Math.min(heat, amount);
        heat -= removed;
        setChanged();
        return removed;
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

    @Override
    public ItemStackHandler getFullItemHandler() {
        return itemHandler;
    }

    public static int toSlotIndex(final int x, final int y) {
        return y * GRID_WIDTH + x;
    }

    public static int slotToX(final int slot) {
        return slot % GRID_WIDTH;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Heat", heat);
        tag.putInt("ChamberCount", chamberCount);
        tag.put("Items", itemHandler.serializeNBT());
        tag.put("SteamTank", steamTank.writeToNBT(new CompoundTag()));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        heat = tag.getDouble("Heat");
        chamberCount = tag.getInt("ChamberCount");
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("SteamTank")) {
            steamTank.readFromNBT(tag.getCompound("SteamTank"));
            steamTank.setFluid(Ic2Fluids.migrateSteamTank(steamTank.getFluid()));
        }
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.steam_reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new SteamReactorMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        Direction output = getBlockState().getValue(SteamReactorBlock.FACING);
        if (capability == ForgeCapabilities.FLUID_HANDLER && side == output) {
            return steamTankOptional.cast();
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
        steamTankOptional.invalidate();
    }
}
