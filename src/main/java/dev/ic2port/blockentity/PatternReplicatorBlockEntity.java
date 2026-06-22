package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.UuMatterItem;
import dev.ic2port.menu.PatternReplicatorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.EnergyOverloadHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.ProcessOnlyItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

/**
 * EV pattern replicator — consumes UU-matter to copy the item in the pattern slot.
 * 1000 EU + 8 UU-matter per item replicated.
 */
public class PatternReplicatorBlockEntity extends BlockEntity implements IEnergyAcceptor, net.minecraft.world.MenuProvider, FullInventoryAccess {

    public static final double ENERGY_CAPACITY = 50_000.0D;
    public static final int TIER = EnergyTier.EV;
    public static final int UU_PER_CRAFT = 8;
    public static final double EU_PER_CRAFT = 1000.0D;
    public static final int REPLICATION_TICKS = 100;

    public static final int SLOT_PATTERN = 0;
    public static final int SLOT_UU_MATTER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override protected void onContentsChanged(final int slot) { setChanged(); }

        @Override
        public @NotNull ItemStack insertItem(final int slot, final @NotNull ItemStack stack, final boolean simulate) {
            if (progress > 0 && slot != SLOT_OUTPUT) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override public boolean isItemValid(final int slot, final ItemStack stack) {
            if (slot == SLOT_UU_MATTER) {
                return stack.getItem() instanceof UuMatterItem;
            }
            if (slot == SLOT_OUTPUT) {
                return false;
            }
            if (slot == SLOT_PATTERN) {
                return !stack.isEmpty() && !(stack.getItem() instanceof UuMatterItem);
            }
            return false;
        }
    };
    private final ProcessOnlyItemHandler automationItemHandler = new ProcessOnlyItemHandler(
            itemHandler, SLOT_COUNT, slot -> slot == SLOT_OUTPUT);
    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> automationItemHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private int progress;
    private boolean destroyedByOverload;
    private ItemStack lockedPattern = ItemStack.EMPTY;

    private final ContainerData data = new ContainerData() {
        @Override public int get(final int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> REPLICATION_TICKS;
                case 2 -> (int) Math.round(storedEnergy);
                case 3 -> (int) Math.round(ENERGY_CAPACITY);
                default -> 0;
            };
        }
        @Override public void set(final int i, final int v) { ContainerDataHelper.ignoreClientWrite(); }
        @Override public int getCount() { return 4; }
    };

    public PatternReplicatorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.PATTERN_REPLICATOR_BE.get(), pos, state);
    }

    public static void serverTick(final Level level, final BlockPos pos, final BlockState state,
                                   final PatternReplicatorBlockEntity entity) {
        entity.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload) return;

        ItemStack pattern = itemHandler.getStackInSlot(SLOT_PATTERN);
        ItemStack uuSlot = itemHandler.getStackInSlot(SLOT_UU_MATTER);
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);

        if (progress > 0 && lockedPattern.isEmpty() && !pattern.isEmpty()) {
            lockedPattern = pattern.copy();
        }

        if (pattern.isEmpty() || uuSlot.getCount() < UU_PER_CRAFT) {
            cancelReplication();
            return;
        }

        if (progress > 0 && !lockedPattern.isEmpty() && !ItemStack.isSameItemSameTags(lockedPattern, pattern)) {
            cancelReplication();
            return;
        }

        ItemStack expectedOutput = progress > 0 && !lockedPattern.isEmpty() ? lockedPattern : pattern;
        if (!output.isEmpty()) {
            if (!ItemStack.isSameItemSameTags(output, expectedOutput)) {
                cancelReplication();
                return;
            }
            if (output.getCount() + 1 > output.getMaxStackSize()) {
                if (progress > 0) {
                    progress = REPLICATION_TICKS - 1;
                }
                setChanged();
                return;
            }
        }

        if (progress == 0) {
            if (storedEnergy < EU_PER_CRAFT) {
                return;
            }
            lockedPattern = pattern.copy();
            storedEnergy -= EU_PER_CRAFT;
        }

        progress++;
        setChanged();

        if (progress >= REPLICATION_TICKS) {
            ItemStack outputNow = itemHandler.getStackInSlot(SLOT_OUTPUT);
            ItemStack uuNow = itemHandler.getStackInSlot(SLOT_UU_MATTER);
            ItemStack patternNow = itemHandler.getStackInSlot(SLOT_PATTERN);
            ItemStack expected = !lockedPattern.isEmpty() ? lockedPattern : patternNow;

            if (patternNow.isEmpty() || uuNow.getCount() < UU_PER_CRAFT) {
                cancelReplication();
                return;
            }
            if (!outputNow.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(outputNow, expected)
                        || outputNow.getCount() + 1 > outputNow.getMaxStackSize()) {
                    progress = REPLICATION_TICKS - 1;
                    setChanged();
                    return;
                }
            }

            uuNow.shrink(UU_PER_CRAFT);
            itemHandler.setStackInSlot(SLOT_UU_MATTER, uuNow);
            ItemStack result = lockedPattern.isEmpty() ? patternNow.copy() : lockedPattern.copy();
            result.setCount(1);
            if (outputNow.isEmpty()) {
                itemHandler.setStackInSlot(SLOT_OUTPUT, result);
            } else {
                outputNow.grow(1);
                itemHandler.setStackInSlot(SLOT_OUTPUT, outputNow);
            }
            progress = 0;
            lockedPattern = ItemStack.EMPTY;
            setChanged();
        }
    }

    private void cancelReplication() {
        if (progress > 0) {
            storedEnergy = Math.min(ENERGY_CAPACITY, storedEnergy + EU_PER_CRAFT);
        }
        progress = 0;
        lockedPattern = ItemStack.EMPTY;
        setChanged();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > getTier()) {
            destroyedByOverload = true;
            EnergyOverloadHelper.tryExplode(level, worldPosition, this, tier, getTier());
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override public double getCapacity() { return ENERGY_CAPACITY; }
    @Override public double getStoredEnergy() { return storedEnergy; }
    @Override public int getTier() { return TIER; }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putInt("Progress", progress);
        if (!lockedPattern.isEmpty()) {
            tag.put("LockedPattern", lockedPattern.save(new CompoundTag()));
        }
        tag.put("Items", itemHandler.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        progress = tag.getInt("Progress");
        lockedPattern = tag.contains("LockedPattern")
                ? ItemStack.of(tag.getCompound("LockedPattern"))
                : ItemStack.EMPTY;
        if (tag.contains("Items")) itemHandler.deserializeNBT(tag.getCompound("Items"));
    }

    public ContainerData getContainerData() { return data; }
    public ItemStackHandler getItemHandler() { return itemHandler; }

    @Override
    public IItemHandler getFullItemHandler() {
        return itemHandler;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> cap,
                                                       final @Nullable Direction side) {
        if (cap == ModCapabilities.ENERGY_NODE_CAPABILITY) return energyOptional.cast();
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemOptional.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.pattern_replicator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory inv, final Player player) {
        return new PatternReplicatorMenu(id, inv, this, data);
    }
}
