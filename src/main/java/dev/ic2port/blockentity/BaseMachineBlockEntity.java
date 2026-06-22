package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.item.IUpgradeItem;
import dev.ic2port.network.ModMessages;
import dev.ic2port.network.packet.EnergySyncS2CPacket;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.recipe.IMachineRecipe;
import dev.ic2port.util.MachineUpgradeMath;
import dev.ic2port.util.ProcessOnlyItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base for IC2 machine block entities.
 * <p>
 * Provides process inventory, four player-only upgrade slots, EU storage, capability exposure and client energy sync.
 */
public abstract class BaseMachineBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider {

    public static final int UPGRADE_SLOT_COUNT = 4;

    public static final double ENERGY_PER_STORAGE_UPGRADE = 10_000.0D;

    private static final double ENERGY_SYNC_EPSILON = 0.001D;

    private final int processSlotCount;
    private final ItemStackHandler itemHandler;
    private final ProcessOnlyItemHandler automationItemHandler;
    private final LazyOptional<IItemHandler> itemHandlerOptional;
    private final LazyOptional<IItemHandler> automationItemHandlerOptional;
    private final LazyOptional<IEnergyNode> energyOptional;

    private final double energyCapacity;

    private double storedEnergy;
    private boolean destroyedByOverload;
    private double lastSyncedEnergy = Double.NaN;
    private double clientStoredEnergy;

    private boolean overclockerLayoutChanged;

    protected BaseMachineBlockEntity(
            final BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state,
            final int processSlotCount,
            final double energyCapacity) {
        super(type, pos, state);
        this.processSlotCount = processSlotCount;
        this.energyCapacity = energyCapacity;
        this.itemHandler = createItemHandler(processSlotCount + UPGRADE_SLOT_COUNT, processSlotCount);
        this.automationItemHandler = new ProcessOnlyItemHandler(itemHandler, processSlotCount);
        this.itemHandlerOptional = LazyOptional.of(() -> itemHandler);
        this.automationItemHandlerOptional = LazyOptional.of(() -> automationItemHandler);
        this.energyOptional = LazyOptional.of(() -> this);
    }

    /**
     * @return {@code true} for slots that accept player/automation input (hoppers, pipes)
     */
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == 0;
    }

    /**
     * @return whether automation may insert the stack into a process input slot
     */
    protected boolean isValidProcessInput(final ItemStack stack) {
        return true;
    }

    /**
     * @return whether automation may insert the stack into the given process slot
     */
    protected boolean isValidProcessInput(final int processSlot, final ItemStack stack) {
        return isValidProcessInput(stack);
    }

    protected ItemStackHandler createItemHandler(final int totalSlots, final int processSlots) {
        return new ItemStackHandler(totalSlots) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack stack) {
                if (slot >= processSlots) {
                    return stack.isEmpty() || stack.getItem() instanceof IUpgradeItem;
                }
                if (!isProcessSlotInput(slot)) {
                    return false;
                }
                return !stack.isEmpty() && isValidProcessInput(slot, stack);
            }

            @Override
            protected void onContentsChanged(final int slot) {
                if (slot >= processSlots) {
                    clampStoredEnergyToCapacity();
                    markUpgradeLayoutChanged();
                }
                setChanged();
            }
        };
    }

    protected boolean consumeOverclockerLayoutReset() {
        if (!overclockerLayoutChanged) {
            return false;
        }
        overclockerLayoutChanged = false;
        return true;
    }

    protected void markUpgradeLayoutChanged() {
        overclockerLayoutChanged = true;
        clampStoredEnergyToCapacity();
    }

    public int getProcessSlotCount() {
        return processSlotCount;
    }

    public int getUpgradeSlotStart() {
        return processSlotCount;
    }

    public int getTotalSlotCount() {
        return processSlotCount + UPGRADE_SLOT_COUNT;
    }

    protected ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public IItemHandler getFullItemHandler() {
        return itemHandler;
    }

    public int countOverclockers() {
        return MachineUpgradeMath.countOverclockers(itemHandler, getUpgradeSlotStart());
    }

    public int countTransformerUpgrades() {
        return MachineUpgradeMath.countTransformerUpgrades(itemHandler, getUpgradeSlotStart());
    }

    public int countEnergyStorageUpgrades() {
        return MachineUpgradeMath.countEnergyStorageUpgrades(itemHandler, getUpgradeSlotStart());
    }

    public double getBaseEnergyCapacity() {
        return energyCapacity;
    }

    /**
     * @return highest incoming EU packet tier this machine can safely accept
     */
    public int getMaxAllowedInputTier() {
        return Math.min(EnergyTier.EV, getTier() + countTransformerUpgrades());
    }

    public int getScaledProcessTime(final int baseProcessTime) {
        return MachineUpgradeMath.scaledProcessTime(baseProcessTime, countOverclockers());
    }

    public double getScaledEnergyPerTick(final double baseEnergyPerTick) {
        return MachineUpgradeMath.scaledEnergyPerTick(baseEnergyPerTick, countOverclockers());
    }

    /**
     * Derives EU/t from recipe total cost and duration, falling back to the machine default.
     */
    protected double getRecipeEnergyPerTick(final IMachineRecipe recipe, final double fallbackPerTick) {
        final int time = recipe.getProcessingTime();
        final double totalEnergy = recipe.getEnergyCost();
        if (time > 0 && totalEnergy > 0.0D) {
            return getScaledEnergyPerTick(totalEnergy / time);
        }
        return getScaledEnergyPerTick(fallbackPerTick);
    }

    @Override
    public double getCapacity() {
        return energyCapacity + countEnergyStorageUpgrades() * ENERGY_PER_STORAGE_UPGRADE;
    }

    protected void clampStoredEnergyToCapacity() {
        double capacity = getCapacity();
        if (storedEnergy > capacity) {
            storedEnergy = capacity;
            syncEnergy();
        }
    }

    @Override
    public double getStoredEnergy() {
        if (level != null && level.isClientSide) {
            return clientStoredEnergy;
        }
        return storedEnergy;
    }

    @Override
    public abstract int getTier();

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > getMaxAllowedInputTier()) {
            explode(tier);
            return amount;
        }

        double space = getCapacity() - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return amount;
        }

        storedEnergy += accepted;
        setChanged();
        syncEnergy();
        return amount - accepted;
    }

    protected void explode(final int incomingTier) {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }
        destroyedByOverload = true;

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        }
        storedEnergy = 0.0D;

        float radius = ModConfig.EXPLOSION_BASE_RADIUS.get().floatValue()
                + (incomingTier - getMaxAllowedInputTier()) * 1.5F;
        double centerX = worldPosition.getX() + 0.5D;
        double centerY = worldPosition.getY() + 0.5D;
        double centerZ = worldPosition.getZ() + 0.5D;

        level.removeBlock(worldPosition, false);
        level.explode(null, centerX, centerY, centerZ, radius, Level.ExplosionInteraction.BLOCK);
    }

    protected boolean consumeEnergy(final double amount) {
        if (amount <= 0.0D || storedEnergy < amount) {
            return false;
        }
        storedEnergy -= amount;
        setChanged();
        syncEnergy();
        return true;
    }

    protected void syncEnergy() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (Math.abs(storedEnergy - lastSyncedEnergy) < ENERGY_SYNC_EPSILON) {
            return;
        }
        lastSyncedEnergy = storedEnergy;
        if (level instanceof ServerLevel serverLevel) {
            ModMessages.sendToClientsTrackingChunk(
                    new EnergySyncS2CPacket(worldPosition, storedEnergy),
                    serverLevel,
                    worldPosition);
        }
    }

    public void setClientStoredEnergy(final double energy) {
        this.clientStoredEnergy = energy;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return automationItemHandlerOptional.cast();
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
        automationItemHandlerOptional.invalidate();
        energyOptional.invalidate();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.put("Inventory", itemHandler.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = tag.getDouble("StoredEnergy");
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        clampStoredEnergyToCapacity();
        lastSyncedEnergy = storedEnergy;
    }

    @Override
    public abstract Component getDisplayName();

    @Nullable
    @Override
    public abstract AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player);
}
