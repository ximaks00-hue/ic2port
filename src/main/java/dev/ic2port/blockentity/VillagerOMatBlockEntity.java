package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.menu.VillagerOMatMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.VillagerOMatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Automated villager trading station — trades once per minute using buffered inputs.
 */
public class VillagerOMatBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider, FullInventoryAccess {

    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 4;
    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_OUTPUT_START = INPUT_SLOTS;
    public static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;

    public static final int SCAN_RADIUS = 16;
    public static final int TRADE_INTERVAL = 1200;
    public static final double EU_PER_ENABLED_TRADE = 6000.0D;
    public static final double ENERGY_CAPACITY = 100_000.0D;
    public static final int TIER = EnergyTier.LV;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return slot < SLOT_OUTPUT_START;
        }
    };

    private final VillagerOMatAutomationHandler automationHandler = new VillagerOMatAutomationHandler(itemHandler);
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> automationHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final List<UUID> trackedVillagers = new ArrayList<>();
    private int selectedVillagerIndex;
    private int enabledTradeMask;
    private int tradeCooldown = TRADE_INTERVAL;
    private int storedXp;
    private double storedEnergy;
    @Nullable
    private UUID ownerUuid;
    private int rescanCooldown;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(storedEnergy, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(ENERGY_CAPACITY, Integer.MAX_VALUE);
                case 2 -> tradeCooldown;
                case 3 -> storedXp;
                case 4 -> selectedVillagerIndex;
                case 5 -> trackedVillagers.size();
                case 6 -> enabledTradeMask;
                case 7 -> getActiveTradeCount();
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public VillagerOMatBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.VILLAGER_O_MAT_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final VillagerOMatBlockEntity mat) {
        mat.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (rescanCooldown-- <= 0) {
            rescanCooldown = 100;
            refreshVillagers();
        }
        if (tradeCooldown > 0) {
            tradeCooldown--;
            return;
        }
        attemptTrades();
        tradeCooldown = TRADE_INTERVAL;
    }

    private void attemptTrades() {
        Villager villager = getSelectedVillager();
        if (villager == null || enabledTradeMask == 0) {
            return;
        }
        Player tradingPlayer = resolveTradingPlayer();
        if (tradingPlayer != null) {
            villager.setTradingPlayer(tradingPlayer);
        }
        MerchantOffers offers = villager.getOffers();
        if (offers == null || offers.isEmpty()) {
            return;
        }
        int enabledCount = Integer.bitCount(enabledTradeMask);
        if (storedEnergy < enabledCount * EU_PER_ENABLED_TRADE) {
            return;
        }

        boolean traded = false;
        for (int index = 0; index < Math.min(offers.size(), VillagerOMatHelper.MAX_TRADES); index++) {
            if ((enabledTradeMask & (1 << index)) == 0) {
                continue;
            }
            MerchantOffer offer = offers.get(index);
            if (VillagerOMatHelper.executeTrade(
                    itemHandler,
                    INPUT_SLOTS,
                    SLOT_OUTPUT_START,
                    OUTPUT_SLOTS,
                    offer)) {
                traded = true;
                storedXp += offer.getXp();
            }
        }
        if (traded) {
            storedEnergy -= enabledCount * EU_PER_ENABLED_TRADE;
            setChanged();
        }
    }

    @Nullable
    private Player resolveTradingPlayer() {
        if (level == null) {
            return null;
        }
        if (ownerUuid != null) {
            Player owner = level.getPlayerByUUID(ownerUuid);
            if (owner != null) {
                return owner;
            }
        }
        return level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                SCAN_RADIUS, false);
    }

    public void refreshVillagers() {
        if (level == null) {
            return;
        }
        trackedVillagers.clear();
        trackedVillagers.addAll(VillagerOMatHelper.scanVillagers(level, worldPosition, SCAN_RADIUS));
        if (selectedVillagerIndex >= trackedVillagers.size()) {
            selectedVillagerIndex = Math.max(0, trackedVillagers.size() - 1);
        }
        setChanged();
    }

    @Nullable
    public Villager getSelectedVillager() {
        if (level == null || trackedVillagers.isEmpty()) {
            return null;
        }
        int index = Math.min(selectedVillagerIndex, trackedVillagers.size() - 1);
        return VillagerOMatHelper.findVillager(level, worldPosition, trackedVillagers.get(index), SCAN_RADIUS);
    }

    public int getActiveTradeCount() {
        Villager villager = getSelectedVillager();
        return Math.min(VillagerOMatHelper.tradeCount(villager), VillagerOMatHelper.MAX_TRADES);
    }

    public void cycleVillager(final int delta) {
        if (trackedVillagers.isEmpty()) {
            return;
        }
        selectedVillagerIndex = Math.floorMod(selectedVillagerIndex + delta, trackedVillagers.size());
        setChanged();
    }

    public void toggleTrade(final int tradeIndex) {
        if (tradeIndex < 0 || tradeIndex >= VillagerOMatHelper.MAX_TRADES) {
            return;
        }
        enabledTradeMask ^= (1 << tradeIndex);
        setChanged();
    }

    public void collectXp(final ServerPlayer player) {
        if (storedXp <= 0) {
            return;
        }
        player.giveExperiencePoints(storedXp);
        storedXp = 0;
        setChanged();
    }

    public void bindOwner(final Player player) {
        if (ownerUuid == null) {
            ownerUuid = player.getUUID();
            setChanged();
        }
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public ItemStackHandler getFullItemHandler() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.villager_o_mat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            bindOwner(player);
            refreshVillagers();
        }
        return new VillagerOMatMenu(containerId, playerInventory, this, data);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        storedEnergy = tag.getDouble("Energy");
        storedXp = tag.getInt("StoredXp");
        selectedVillagerIndex = tag.getInt("SelectedVillager");
        enabledTradeMask = tag.getInt("EnabledTrades");
        tradeCooldown = tag.getInt("TradeCooldown");
        if (tag.hasUUID("Owner")) {
            ownerUuid = tag.getUUID("Owner");
        }
        trackedVillagers.clear();
        ListTag list = tag.getList("Villagers", Tag.TAG_COMPOUND);
        for (Tag entry : list) {
            if (entry instanceof CompoundTag compound && compound.hasUUID("Id")) {
                trackedVillagers.add(compound.getUUID("Id"));
            }
        }
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putDouble("Energy", storedEnergy);
        tag.putInt("StoredXp", storedXp);
        tag.putInt("SelectedVillager", selectedVillagerIndex);
        tag.putInt("EnabledTrades", enabledTradeMask);
        tag.putInt("TradeCooldown", tradeCooldown);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        ListTag list = new ListTag();
        for (UUID uuid : trackedVillagers) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", uuid);
            list.add(entry);
        }
        tag.put("Villagers", list);
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

    private static final class VillagerOMatAutomationHandler implements IItemHandlerModifiable {

        private final ItemStackHandler delegate;

        private VillagerOMatAutomationHandler(final ItemStackHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return TOTAL_SLOTS;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(final int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(final int slot, final @NotNull ItemStack stack) {
            delegate.setStackInSlot(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(final int slot, final @NotNull ItemStack stack, final boolean simulate) {
            if (slot >= SLOT_OUTPUT_START) {
                return stack;
            }
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
            if (slot < SLOT_OUTPUT_START) {
                return ItemStack.EMPTY;
            }
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
