package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyEmitter;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.block.MFSUBlock;
import dev.ic2port.menu.MFSUMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.EnergyStorageExplosionHelper;
import dev.ic2port.util.ContainerDataHelper;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MFSUBlockEntity extends BlockEntity implements IEnergyAcceptor, IEnergyEmitter, MenuProvider {

    public static final double MAX_OUTPUT_PER_TICK = 512.0D;
    public static final int TIER = EnergyTier.HV;
    public static final int SLOT_CHARGE = 0;
    public static final int SLOT_DISCHARGE = 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(storedEnergy, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(getEnergyCapacity(), Integer.MAX_VALUE);
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private double storedEnergy;
    private boolean destroyedByOverload;

    public MFSUBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MFSU_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MFSUBlockEntity mfsu) {
        mfsu.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }

        double outputBudget = MAX_OUTPUT_PER_TICK;
        double remaining = processItemSlots(outputBudget);

        if (storedEnergy <= 0.0D || remaining <= 0.0D) {
            return;
        }

        Direction outputDirection = getBlockState().getValue(MFSUBlock.FACING);
        double offered = Math.min(storedEnergy, remaining);
        double remainder = EnergyTransferHelper.injectIntoNeighbor(level, worldPosition, outputDirection, offered, TIER);
        double transferred = offered - remainder;
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
        }
    }

    private double processItemSlots(final double outputBudget) {
        double remaining = outputBudget;

        ItemStack chargeStack = itemHandler.getStackInSlot(SLOT_CHARGE);
        if (!chargeStack.isEmpty() && storedEnergy > 0.0D && remaining > 0.0D
                && ItemEnergyHelper.canCharge(chargeStack, TIER)) {
            double toTransfer = Math.min(Math.min(storedEnergy, remaining), MAX_OUTPUT_PER_TICK);
            double transferred = ItemEnergyHelper.chargeItem(chargeStack, toTransfer, TIER);
            if (transferred > 0.0D) {
                storedEnergy -= transferred;
                remaining -= transferred;
                setChanged();
            }
        }

        ItemStack dischargeStack = itemHandler.getStackInSlot(SLOT_DISCHARGE);
        if (!dischargeStack.isEmpty() && storedEnergy < getEnergyCapacity()
                && ItemEnergyHelper.canDischargeInto(dischargeStack, TIER)) {
            double space = getEnergyCapacity() - storedEnergy;
            double toDraw = Math.min(space, MAX_OUTPUT_PER_TICK);
            double drawn = ItemEnergyHelper.dischargeItemAndModules(dischargeStack, toDraw, TIER);
            if (drawn > 0.0D) {
                storedEnergy += drawn;
                setChanged();
            }
        }
        return remaining;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > getTier()) {
            explode(tier);
            return amount;
        }

        Direction outputDirection = getBlockState().getValue(MFSUBlock.FACING);
        if (directionFrom == outputDirection) {
            return amount;
        }

        double space = getEnergyCapacity() - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return amount;
        }

        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    private void explode(final int incomingTier) {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }
        destroyedByOverload = true;
        storedEnergy = 0.0D;
        EnergyStorageExplosionHelper.explode(level, worldPosition, itemHandler, getTier(), incomingTier);
    }

    public static double getEnergyCapacity() {
        return ModConfig.MFSU_CAPACITY.get();
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

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), getEnergyCapacity());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.mfsu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MFSUMenu(containerId, playerInventory, this, data);
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
