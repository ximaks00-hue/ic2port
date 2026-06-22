package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.menu.FusionReactorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.BlockEntitySpillHelper;
import dev.ic2port.util.EnergyOverloadHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.FusionFuelHelper;
import dev.ic2port.util.FusionMeltableHelper;
import dev.ic2port.util.FusionReactorHelper;
import dev.ic2port.util.InsertOnlyFluidHandler;
import dev.ic2port.util.ProcessOnlyItemHandler;
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
import net.minecraft.util.Mth;
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
 * Thermonuclear reactor controller — 5×5×5 reinforced shell, produces lava from uranium rods.
 */
public class FusionReactorBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider, FullInventoryAccess {

    public static final int FUEL_SLOT_START = 0;
    public static final int FUEL_SLOT_END = 5;
    public static final int MELTABLE_SLOT = 6;
    public static final int SLOT_COUNT = 7;

    public static final int LAVA_CAPACITY_MB = 32_000;
    public static final double ENERGY_CAPACITY = 500_000.0D;
    public static final double HEAT_TARGET = 200_000.0D;
    public static final double HEAT_EU_PER_TICK = 128.0D;
    public static final int TIER = EnergyTier.HV;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (slot >= FUEL_SLOT_START && slot <= FUEL_SLOT_END) {
                return stack.is(ItemRegistry.FUEL_ROD.get()) || stack.is(ItemRegistry.MOX_FUEL_ROD.get());
            }
            if (slot == MELTABLE_SLOT) {
                return FusionMeltableHelper.isMeltable(stack);
            }
            return false;
        }

        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };

    private final ProcessOnlyItemHandler automationItemHandler = new ProcessOnlyItemHandler(
            itemHandler, SLOT_COUNT, slot -> false);
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> automationItemHandler);

    private final FluidTank lavaTank = new FluidTank(LAVA_CAPACITY_MB, fluid -> fluid.getFluid().isSame(Fluids.LAVA)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final LazyOptional<IFluidHandler> fluidOptional =
            LazyOptional.of(() -> new InsertOnlyFluidHandler(lavaTank));
    private final LazyOptional<IEnergyAcceptor> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(heat, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(HEAT_TARGET, Integer.MAX_VALUE);
                case 2 -> lavaTank.getFluidAmount();
                case 3 -> LAVA_CAPACITY_MB;
                case 4 -> structureValid ? 1 : 0;
                case 5 -> (int) Math.min(storedEnergy, Integer.MAX_VALUE);
                case 6 -> (int) Math.min(ENERGY_CAPACITY, Integer.MAX_VALUE);
                case 7 -> comparatorHeatMode ? 1 : 0;
                case 8 -> autoExportLava ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    private double storedEnergy;
    private double heat;
    private boolean structureValid;
    private boolean comparatorHeatMode;
    private boolean autoExportLava = true;
    private int validationCooldown;
    private int productionCooldown;
    private int fuelConsumeCooldown;
    private boolean meltdownTriggered;
    private boolean destroyedByOverload;

    public FusionReactorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.FUSION_REACTOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final FusionReactorBlockEntity reactor) {
        reactor.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || meltdownTriggered || destroyedByOverload) {
            return;
        }

        if (autoExportLava && lavaTank.getFluidAmount() > 0) {
            tryExportLava();
        }

        if (validationCooldown-- <= 0) {
            validationCooldown = 40;
            boolean wasValid = structureValid;
            structureValid = FusionReactorHelper.validateStructure(level, worldPosition);
            if (wasValid && !structureValid && !meltdownTriggered && shouldMeltdown()) {
                triggerMeltdown();
                return;
            }
            notifyComparatorOutput();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (!structureValid) {
            if (heat > 0.0D) {
                heat = Math.max(0.0D, heat - 4.0D);
            }
            setChanged();
            return;
        }

        if (heat < HEAT_TARGET) {
            final double heatEuPerTick = ModConfig.FUSION_HEAT_EU_PER_TICK.get();
            if (storedEnergy >= heatEuPerTick) {
                storedEnergy -= heatEuPerTick;
                heat += heatEuPerTick;
            }
            notifyComparatorOutput();
            setChanged();
            return;
        }

        if (productionCooldown > 0) {
            productionCooldown--;
            setChanged();
            return;
        }

        int baseProduced = (int) Math.round(
                FusionFuelHelper.countProductionRate(itemHandler, FUEL_SLOT_START, FUEL_SLOT_END)
                        * ModConfig.FUSION_LAVA_MULTIPLIER.get());
        if (baseProduced <= 0 || lavaTank.getFluidAmount() >= LAVA_CAPACITY_MB) {
            setChanged();
            return;
        }

        int tankSpace = lavaTank.getCapacity() - lavaTank.getFluidAmount();
        int fillBase = Math.min(baseProduced, tankSpace);
        int fillBonus = 0;
        ItemStack meltable = itemHandler.getStackInSlot(MELTABLE_SLOT);
        if (!meltable.isEmpty()) {
            int bonusMb = FusionMeltableHelper.getBonusMb(meltable);
            if (tankSpace - fillBase >= bonusMb) {
                fillBonus = bonusMb;
            }
        }
        int totalFill = fillBase + fillBonus;
        if (totalFill <= 0) {
            setChanged();
            return;
        }

        productionCooldown = FusionFuelHelper.PRODUCTION_INTERVAL_TICKS;

        lavaTank.fill(new FluidStack(Fluids.LAVA, totalFill), IFluidHandler.FluidAction.EXECUTE);

        if (fillBonus > 0) {
            meltable.shrink(1);
            itemHandler.setStackInSlot(MELTABLE_SLOT, meltable);
        }

        if (fuelConsumeCooldown <= 0) {
            fuelConsumeCooldown = FusionFuelHelper.FUEL_CONSUME_INTERVAL_TICKS;
            consumePartialFuelRod();
        } else {
            fuelConsumeCooldown--;
        }
        notifyComparatorOutput();
        setChanged();
    }

    private void tryExportLava() {
        if (level == null || lavaTank.getFluidAmount() <= 0) {
            return;
        }

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
            int drained = lavaTank.drain(500, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained <= 0) {
                continue;
            }
            FluidStack toFill = lavaTank.drain(drained, IFluidHandler.FluidAction.EXECUTE);
            int filled = handler.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                if (filled < toFill.getAmount()) {
                    lavaTank.fill(
                            new FluidStack(toFill.getFluid(), toFill.getAmount() - filled),
                            IFluidHandler.FluidAction.EXECUTE);
                }
                setChanged();
                return;
            }
            lavaTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private boolean shouldMeltdown() {
        if (heat < HEAT_TARGET * 0.5D) {
            return false;
        }
        int rods = FusionReactorHelper.countFuelRods(itemHandler, FUEL_SLOT_START, FUEL_SLOT_END);
        return rods > 0 || lavaTank.getFluidAmount() >= 1_000;
    }

    private void triggerMeltdown() {
        if (level == null || meltdownTriggered) {
            return;
        }
        meltdownTriggered = true;
        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;
        int lavaMb = lavaTank.getFluidAmount();
        BlockEntitySpillHelper.spillItems(level, worldPosition, itemHandler);
        BlockEntitySpillHelper.spillFluids(level, worldPosition, this);
        level.removeBlock(worldPosition, false);
        level.explode(null, centerX, centerY, centerZ, 8.0F, Level.ExplosionInteraction.BLOCK);
        if (lavaMb > 0) {
            level.explode(null, centerX, centerY, centerZ, 4.0F, Level.ExplosionInteraction.NONE);
        }
        lavaTank.setFluid(FluidStack.EMPTY);
        heat = 0.0D;
        storedEnergy = 0.0D;
    }

    private void consumePartialFuelRod() {
        for (int slot = FUEL_SLOT_START; slot <= FUEL_SLOT_END; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(ItemRegistry.FUEL_ROD.get()) && !stack.is(ItemRegistry.MOX_FUEL_ROD.get())) {
                continue;
            }
            stack.shrink(1);
            ItemStack depleted = new ItemStack(ItemRegistry.DEPLETED_FUEL_ROD.get());
            if (stack.isEmpty()) {
                itemHandler.setStackInSlot(slot, depleted);
            } else {
                itemHandler.setStackInSlot(slot, stack);
                insertDepletedRod(depleted);
            }
            return;
        }
    }

    private void insertDepletedRod(final ItemStack depleted) {
        for (int slot = FUEL_SLOT_START; slot <= FUEL_SLOT_END; slot++) {
            ItemStack existing = itemHandler.getStackInSlot(slot);
            if (existing.isEmpty()) {
                itemHandler.setStackInSlot(slot, depleted);
                return;
            }
            if (existing.is(ItemRegistry.DEPLETED_FUEL_ROD.get())
                    && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(1);
                itemHandler.setStackInSlot(slot, existing);
                return;
            }
        }
        if (level != null) {
            Block.popResource(level, worldPosition, depleted);
        }
    }

    public FluidTank getLavaTank() {
        return lavaTank;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    public int getComparatorOutput() {
        if (!structureValid) {
            return 0;
        }
        if (comparatorHeatMode) {
            if (heat <= 0.0D) {
                return 0;
            }
            return Mth.floor((float) (heat / HEAT_TARGET) * 14.0F) + 1;
        }
        int lava = lavaTank.getFluidAmount();
        if (lava <= 0) {
            return 0;
        }
        return Mth.floor((float) lava / LAVA_CAPACITY_MB * 14.0F) + 1;
    }

    public boolean isComparatorHeatMode() {
        return comparatorHeatMode;
    }

    public void toggleComparatorHeatMode() {
        comparatorHeatMode = !comparatorHeatMode;
        notifyComparatorOutput();
        setChanged();
    }

    public boolean isAutoExportLava() {
        return autoExportLava;
    }

    public void toggleAutoExportLava() {
        autoExportLava = !autoExportLava;
        setChanged();
    }

    public void notifyComparatorOutput() {
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public IItemHandler getFullItemHandler() {
        return itemHandler;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D
                || destroyedByOverload || meltdownTriggered) {
            return amount;
        }
        if (tier > getTier()) {
            destroyedByOverload = true;
            EnergyOverloadHelper.tryExplode(level, worldPosition, this, tier, getTier());
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted > 0.0D) {
            storedEnergy += accepted;
            setChanged();
        }
        return amount - accepted;
    }

    @Override
    public int getTier() {
        return TIER;
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
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.fusion_reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new FusionReactorMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.put("LavaTank", lavaTank.writeToNBT(new CompoundTag()));
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putDouble("Heat", heat);
        tag.putBoolean("StructureValid", structureValid);
        tag.putBoolean("MeltdownTriggered", meltdownTriggered);
        tag.putInt("ProductionCooldown", productionCooldown);
        tag.putInt("FuelConsumeCooldown", fuelConsumeCooldown);
        tag.putBoolean("ComparatorHeatMode", comparatorHeatMode);
        tag.putBoolean("AutoExportLava", autoExportLava);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        if (tag.contains("LavaTank")) {
            lavaTank.readFromNBT(tag.getCompound("LavaTank"));
        }
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        heat = tag.getDouble("Heat");
        structureValid = tag.getBoolean("StructureValid");
        meltdownTriggered = tag.getBoolean("MeltdownTriggered");
        productionCooldown = tag.getInt("ProductionCooldown");
        fuelConsumeCooldown = tag.getInt("FuelConsumeCooldown");
        comparatorHeatMode = tag.getBoolean("ComparatorHeatMode");
        autoExportLava = !tag.contains("AutoExportLava") || tag.getBoolean("AutoExportLava");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> cap,
            final @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidOptional.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        if (cap == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidOptional.invalidate();
        energyOptional.invalidate();
        itemHandlerOptional.invalidate();
    }
}
