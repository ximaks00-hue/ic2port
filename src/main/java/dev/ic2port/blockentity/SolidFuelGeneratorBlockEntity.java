package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.block.SolidFuelGeneratorBlock;
import dev.ic2port.menu.SolidFuelGeneratorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyTransferHelper;
import dev.ic2port.util.ItemEnergyHelper;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LV solid-fuel EU generator — burns vanilla fuel items and outputs 10 EU/t into an internal buffer.
 */
public class SolidFuelGeneratorBlockEntity extends BlockEntity implements IEnergyEmitter, MenuProvider {

    public static final double GENERATION_PER_TICK = 10.0D;
    public static final int TIER = EnergyTier.LV;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_DISCHARGE = 1;
    public static final int SLOT_COUNT = 2;
    private static final double BATTERY_DISCHARGE_PER_TICK = 32.0D;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (slot == SLOT_FUEL) {
                return ForgeHooks.getBurnTime(stack, null) > 0;
            }
            if (slot == SLOT_DISCHARGE) {
                return ItemEnergyHelper.isValidDischargeSlot(stack, TIER);
            }
            return false;
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEnergyEmitter> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.round(storedEnergy);
                case 1 -> (int) Math.round(getEnergyCapacity());
                case 2 -> burnTime;
                case 3 -> totalBurnTime;
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

    private int burnTime;
    private int totalBurnTime;
    private double storedEnergy;

    public SolidFuelGeneratorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.SOLID_FUEL_GENERATOR_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final SolidFuelGeneratorBlockEntity generator) {
        generator.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (burnTime > 0) {
            if (storedEnergy < getEnergyCapacity()) {
                burnTime--;
                produceEnergy();
            }
        } else if (storedEnergy < getEnergyCapacity()) {
            tryConsumeFuel();
        }

        updateLitState(burnTime > 0);
        processBatterySlot();
        distributeEnergy();
    }

    private void processBatterySlot() {
        ItemStack dischargeStack = itemHandler.getStackInSlot(SLOT_DISCHARGE);
        if (dischargeStack.isEmpty() || storedEnergy >= getEnergyCapacity()
                || !ItemEnergyHelper.canDischargeInto(dischargeStack, TIER)) {
            return;
        }

        double space = getEnergyCapacity() - storedEnergy;
        double toDraw = Math.min(space, BATTERY_DISCHARGE_PER_TICK);
        double drawn = ItemEnergyHelper.dischargeItem(dischargeStack, toDraw);
        if (drawn > 0.0D) {
            storedEnergy += drawn;
            setChanged();
        }
    }

    private void produceEnergy() {
        double space = getEnergyCapacity() - storedEnergy;
        if (space <= 0.0D) {
            return;
        }
        double produced = Math.min(GENERATION_PER_TICK, space);
        storedEnergy += produced;
        setChanged();
    }

    private void tryConsumeFuel() {
        ItemStack fuelStack = itemHandler.getStackInSlot(SLOT_FUEL);
        if (fuelStack.isEmpty()) {
            return;
        }

        int fuelBurnTime = ForgeHooks.getBurnTime(fuelStack, null);
        if (fuelBurnTime <= 0) {
            return;
        }

        fuelStack.shrink(1);
        totalBurnTime = fuelBurnTime;
        burnTime = fuelBurnTime;
        setChanged();
    }

    private void distributeEnergy() {
        if (storedEnergy <= 0.0D) {
            return;
        }

        double remaining = Math.min(storedEnergy, GENERATION_PER_TICK);
        for (Direction direction : Direction.values()) {
            if (remaining <= 0.0D) {
                break;
            }
            double notAccepted = EnergyTransferHelper.injectIntoNeighbor(
                    level, worldPosition, direction, remaining, TIER);
            double transferred = remaining - notAccepted;
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                remaining = notAccepted;
                setChanged();
            }
        }
    }

    private void updateLitState(final boolean lit) {
        if (level == null || level.isClientSide) {
            return;
        }

        BlockState currentState = getBlockState();
        if (currentState.getValue(SolidFuelGeneratorBlock.LIT) != lit) {
            level.setBlock(worldPosition, currentState.setValue(SolidFuelGeneratorBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    public static double getEnergyCapacity() {
        return ModConfig.SOLID_FUEL_GENERATOR_CAPACITY.get();
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

    public ContainerData getContainerData() {
        return data;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("BurnTime", burnTime);
        tag.putInt("TotalBurnTime", totalBurnTime);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        burnTime = tag.getInt("BurnTime");
        totalBurnTime = tag.getInt("TotalBurnTime");
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), getEnergyCapacity());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.solid_fuel_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new SolidFuelGeneratorMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
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
        energyOptional.invalidate();
    }
}
