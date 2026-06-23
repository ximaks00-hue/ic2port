package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.menu.OreScannerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.EnergyOverloadHelper;
import dev.ic2port.util.OreScannerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Stationary ore scanner — scans underground ores at 40 EU per block (IC2 Classic ore scanner).
 */
public class OreScannerBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider {

    public static final double ENERGY_CAPACITY = 100_000.0D;
    public static final double ENERGY_PER_BLOCK = 40.0D;
    public static final int SCAN_RADIUS = 8;
    public static final int SCAN_DEPTH = 48;
    public static final int TIER = EnergyTier.MV;

    private final LazyOptional<OreScannerBlockEntity> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(Math.round(storedEnergy), Integer.MAX_VALUE);
                case 1 -> (int) Math.min(Math.round(ENERGY_CAPACITY), Integer.MAX_VALUE);
                case 2 -> scanning ? 1 : 0;
                case 3 -> scanProgress;
                case 4 -> scanTotal;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private double storedEnergy;
    private boolean scanning;
    private int scanProgress;
    private int scanTotal;
    private boolean destroyedByOverload;
  @Nullable
    private ServerPlayer lastOperator;

    public OreScannerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.ORE_SCANNER_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final OreScannerBlockEntity scanner) {
        scanner.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload || !scanning) {
            return;
        }
        if (storedEnergy < ENERGY_PER_BLOCK) {
            finishScan();
            return;
        }

        storedEnergy -= ENERGY_PER_BLOCK;
        scanProgress++;
        setChanged();

        if (scanProgress >= scanTotal) {
            Map<String, Integer> counts = OreScannerHelper.scanColumn(level, worldPosition);
            if (lastOperator != null) {
                if (counts.isEmpty()) {
                    lastOperator.displayClientMessage(
                            Component.translatable("message.ic2port.od_scanner.empty"), true);
                } else {
                    lastOperator.displayClientMessage(OreScannerHelper.formatResult(counts), true);
                }
            }
            finishScan();
        }
    }

    public boolean startScan(final Player player) {
        if (level == null || level.isClientSide || scanning || storedEnergy < ENERGY_PER_BLOCK) {
            return false;
        }
        int blocks = (SCAN_RADIUS * 2 + 1) * (SCAN_RADIUS * 2 + 1) * SCAN_DEPTH;
        if (storedEnergy < blocks * ENERGY_PER_BLOCK) {
            return false;
        }
        scanning = true;
        scanProgress = 0;
        scanTotal = blocks;
        if (player instanceof ServerPlayer serverPlayer) {
            lastOperator = serverPlayer;
        }
        setChanged();
        return true;
    }

    private void finishScan() {
        scanning = false;
        scanProgress = 0;
        scanTotal = 0;
        setChanged();
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > getTier()) {
            destroyedByOverload = EnergyOverloadHelper.tryExplode(level, worldPosition, this, tier, getTier());
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

    public ContainerData getContainerData() {
        return data;
    }

    public boolean isScanning() {
        return scanning;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.ore_scanner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new OreScannerMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putBoolean("Scanning", scanning);
        tag.putInt("ScanProgress", scanProgress);
        tag.putInt("ScanTotal", scanTotal);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        scanning = tag.getBoolean("Scanning");
        scanProgress = tag.getInt("ScanProgress");
        scanTotal = tag.getInt("ScanTotal");
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
