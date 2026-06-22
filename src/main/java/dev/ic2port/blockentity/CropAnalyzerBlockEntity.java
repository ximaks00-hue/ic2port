package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.menu.CropAnalyzerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.StationaryCropAnalyzerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MV stationary crop analyzer — scans crops in a 9×5×9 area at 1 EU/t (2500 EU per scan level).
 */
public class CropAnalyzerBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider {

    public static final double ENERGY_CAPACITY = 50_000.0D;
    public static final int TIER = EnergyTier.MV;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(Math.round(storedEnergy), Integer.MAX_VALUE);
                case 1 -> (int) Math.min(Math.round(ENERGY_CAPACITY), Integer.MAX_VALUE);
                case 2 -> (int) Math.min(Math.round(scanProgress), Integer.MAX_VALUE);
                case 3 -> (int) StationaryCropAnalyzerHelper.ENERGY_PER_SCAN_LEVEL;
                case 4 -> hasTarget ? 1 : 0;
                case 5 -> getTargetScanLevel();
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

    private double storedEnergy;
    private BlockPos targetPos = BlockPos.ZERO;
    private boolean hasTarget;
    private double scanProgress;

    public CropAnalyzerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CROP_ANALYZER_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final CropAnalyzerBlockEntity analyzer) {
        analyzer.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (storedEnergy < StationaryCropAnalyzerHelper.ENERGY_PER_TICK) {
            return;
        }

        CropSticksBlockEntity crop = resolveTarget();
        if (crop == null) {
            if (hasTarget || scanProgress > 0.0D) {
                hasTarget = false;
                scanProgress = 0.0D;
                setChanged();
            }
            return;
        }

        storedEnergy -= StationaryCropAnalyzerHelper.ENERGY_PER_TICK;
        scanProgress += StationaryCropAnalyzerHelper.ENERGY_PER_TICK;
        if (scanProgress >= StationaryCropAnalyzerHelper.ENERGY_PER_SCAN_LEVEL) {
            scanProgress -= StationaryCropAnalyzerHelper.ENERGY_PER_SCAN_LEVEL;
            if (StationaryCropAnalyzerHelper.advanceScan(crop)) {
                if (crop.getScanLevel() >= 4) {
                    hasTarget = false;
                    scanProgress = 0.0D;
                }
            } else {
                hasTarget = false;
                scanProgress = 0.0D;
            }
        }
        setChanged();
    }

    @Nullable
    private CropSticksBlockEntity resolveTarget() {
        if (level == null) {
            return null;
        }
        if (hasTarget) {
            BlockEntity blockEntity = level.getBlockEntity(targetPos);
            if (blockEntity instanceof CropSticksBlockEntity crop
                    && crop.getCrop() != null
                    && crop.getScanLevel() < 4) {
                return crop;
            }
            hasTarget = false;
            scanProgress = 0.0D;
        }
        CropSticksBlockEntity next = StationaryCropAnalyzerHelper.findNextCrop(level, worldPosition);
        if (next == null) {
            return null;
        }
        targetPos = next.getBlockPos();
        hasTarget = true;
        return next;
    }

    public boolean isScanning() {
        return hasTarget && scanProgress > 0.0D;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public double getScanProgress() {
        return scanProgress;
    }

    public int getTargetScanLevel() {
        if (level == null || !hasTarget) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (blockEntity instanceof CropSticksBlockEntity crop) {
            return crop.getScanLevel();
        }
        return 0;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.crop_analyzer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new CropAnalyzerMenu(containerId, playerInventory, this, data);
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > TIER) {
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted <= 0.0D) {
            return amount;
        }
        storedEnergy += accepted;
        setChanged();
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

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
        tag.putBoolean("HasTarget", hasTarget);
        tag.putLong("TargetPos", targetPos.asLong());
        tag.putDouble("ScanProgress", scanProgress);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        hasTarget = tag.getBoolean("HasTarget");
        targetPos = BlockPos.of(tag.getLong("TargetPos"));
        scanProgress = tag.getDouble("ScanProgress");
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
