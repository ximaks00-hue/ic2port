package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.ExportUpgradeItem;
import dev.ic2port.item.ImportUpgradeItem;
import dev.ic2port.item.ITransportUpgrade;
import dev.ic2port.menu.MachineBufferMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.OutputBufferHelper;
import dev.ic2port.util.TransportUpgradeHelper;
import dev.ic2port.util.TubeConnectionHelper;
import dev.ic2port.util.TubeTransferHelper;
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

import java.util.List;

/**
 * Nine-slot item buffer with import/export upgrade automation.
 */
public class MachineBufferBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider, FullInventoryAccess {

    public static final int BUFFER_SLOTS = 9;
    public static final int UPGRADE_SLOTS = 4;
    public static final int TOTAL_SLOTS = BUFFER_SLOTS + UPGRADE_SLOTS;
    public static final int UPGRADE_SLOT_START = BUFFER_SLOTS;

    public static final double ENERGY_CAPACITY = 10_000.0D;
    public static final double EU_PER_TRANSFER = 32.0D;
    public static final int TIER = EnergyTier.LV;
    public static final int TICK_INTERVAL = 10;
    public static final int STACKS_PER_TRANSFER = 4;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            if (slot >= UPGRADE_SLOT_START) {
                return stack.isEmpty() || stack.getItem() instanceof ITransportUpgrade;
            }
            return true;
        }
    };

    private final BufferAutomationHandler automationHandler = new BufferAutomationHandler(itemHandler);
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> automationHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(storedEnergy, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(ENERGY_CAPACITY, Integer.MAX_VALUE);
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
    private int tickCooldown;

    public MachineBufferBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MACHINE_BUFFER_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MachineBufferBlockEntity buffer) {
        buffer.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (tickCooldown > 0) {
            tickCooldown--;
            return;
        }
        tickCooldown = TICK_INTERVAL;
        if (storedEnergy < EU_PER_TRANSFER) {
            return;
        }
        for (int slot = UPGRADE_SLOT_START; slot < TOTAL_SLOTS; slot++) {
            ItemStack upgrade = itemHandler.getStackInSlot(slot);
            if (upgrade.isEmpty()) {
                continue;
            }
            if (upgrade.getItem() instanceof ImportUpgradeItem) {
                if (runImport(upgrade)) {
                    storedEnergy -= EU_PER_TRANSFER;
                    setChanged();
                }
            } else if (upgrade.getItem() instanceof ExportUpgradeItem) {
                if (runExport(upgrade)) {
                    storedEnergy -= EU_PER_TRANSFER;
                    setChanged();
                }
            }
        }
    }

    private boolean runImport(final ItemStack upgrade) {
        ItemStack filter = TransportUpgradeHelper.getFilter(upgrade);
        Direction side = TransportUpgradeHelper.getSide(upgrade);
        Iterable<Direction> directions = side != null ? List.of(side) : List.of(Direction.values());
        for (Direction direction : directions) {
            BlockPos neighborPos = worldPosition.relative(direction);
            IItemHandler neighbor = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
            if (neighbor == null) {
                continue;
            }
            int movedStacks = 0;
            while (movedStacks < STACKS_PER_TRANSFER && storedEnergy >= EU_PER_TRANSFER) {
                ItemStack extracted = TubeTransferHelper.extractMatchingItem(neighbor, filter);
                if (extracted.isEmpty()) {
                    break;
                }
                ItemStack remainder = OutputBufferHelper.insert(itemHandler, extracted);
                if (remainder.getCount() == extracted.getCount()) {
                    break;
                }
                movedStacks++;
            }
            if (movedStacks > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean runExport(final ItemStack upgrade) {
        ItemStack filter = TransportUpgradeHelper.getFilter(upgrade);
        Direction side = TransportUpgradeHelper.getSide(upgrade);
        Iterable<Direction> directions = side != null ? List.of(side) : List.of(Direction.values());
        for (Direction direction : directions) {
            BlockPos neighborPos = worldPosition.relative(direction);
            IItemHandler neighbor = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
            if (neighbor == null) {
                continue;
            }
            int movedStacks = 0;
            while (movedStacks < STACKS_PER_TRANSFER && storedEnergy >= EU_PER_TRANSFER) {
                ItemStack extracted = extractFromBuffer(filter);
                if (extracted.isEmpty()) {
                    break;
                }
                ItemStack remainder = TubeTransferHelper.insertIntoHandler(neighbor, extracted);
                if (remainder.getCount() == extracted.getCount()) {
                    insertBack(extracted);
                    break;
                }
                if (!remainder.isEmpty()) {
                    insertBack(remainder);
                }
                movedStacks++;
            }
            if (movedStacks > 0) {
                return true;
            }
        }
        return false;
    }

    private ItemStack extractFromBuffer(final ItemStack filter) {
        for (int slot = 0; slot < BUFFER_SLOTS; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!filter.isEmpty() && !TubeTransferHelper.matchesFilter(filter, stack)) {
                continue;
            }
            ItemStack extracted = itemHandler.extractItem(slot, stack.getMaxStackSize(), false);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    private void insertBack(final ItemStack stack) {
        ItemStack remainder = OutputBufferHelper.insert(itemHandler, stack);
        if (!remainder.isEmpty() && level != null) {
            net.minecraft.world.level.block.Block.popResource(level, worldPosition, remainder);
        }
    }

    @Override
    public ItemStackHandler getFullItemHandler() {
        return itemHandler;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.machine_buffer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MachineBufferMenu(containerId, playerInventory, this, data);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        storedEnergy = tag.getDouble("Energy");
        tickCooldown = tag.getInt("TickCooldown");
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putDouble("Energy", storedEnergy);
        tag.putInt("TickCooldown", tickCooldown);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final Capability<T> capability,
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
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (tier > TIER) {
            return amount;
        }
        double accepted = Math.min(amount, ENERGY_CAPACITY - storedEnergy);
        if (accepted > 0.0D) {
            storedEnergy += accepted;
            setChanged();
        }
        return amount - accepted;
    }

    private static final class BufferAutomationHandler implements IItemHandler {

        private final ItemStackHandler delegate;

        private BufferAutomationHandler(final ItemStackHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return BUFFER_SLOTS;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(final int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(final int slot, final @NotNull ItemStack stack, final boolean simulate) {
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            return delegate.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(final int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return delegate.isItemValid(slot, stack);
        }
    }
}
