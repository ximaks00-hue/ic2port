package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.menu.MinerMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.EnergyOverloadHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.MinerHelper;
import dev.ic2port.util.MinerHelper.DrillProfile;
import dev.ic2port.util.MinerHelper.ScannerMode;
import dev.ic2port.util.OutputBufferHelper;
import dev.ic2port.util.ProcessOnlyItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HV miner — requires a drill, mining pipes and EU. Optional scanner changes targeting behaviour.
 */
public class MinerBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider, FullInventoryAccess {

    public static final double ENERGY_CAPACITY = 20_000.0D;
    public static final double EU_PER_BLOCK = 100.0D;
    public static final int TIER = EnergyTier.HV;

    public static final int SLOT_DRILL = 0;
    public static final int SLOT_SCANNER = 1;
    public static final int SLOT_PIPE = 2;
    public static final int SLOT_OUTPUT_START = 3;
    public static final int OUTPUT_SLOTS = 9;
    public static final int SLOT_COUNT = SLOT_OUTPUT_START + OUTPUT_SLOTS;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return switch (slot) {
                case SLOT_DRILL -> MinerHelper.isValidDrill(stack);
                case SLOT_SCANNER -> MinerHelper.isValidScanner(stack);
                case SLOT_PIPE -> MinerHelper.isMiningPipe(stack);
                default -> false;
            };
        }
    };
    private final ProcessOnlyItemHandler automationHandler = new ProcessOnlyItemHandler(
            itemHandler, SLOT_COUNT, slot -> slot >= SLOT_OUTPUT_START);
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> automationOptional = LazyOptional.of(() -> automationHandler);
    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.round(storedEnergy);
                case 1 -> (int) Math.round(ENERGY_CAPACITY);
                case 2 -> done ? 1 : 0;
                case 3 -> mineY;
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

    private double storedEnergy;
    private int mineY;
    private int tickCount;
    private boolean done;
    private boolean destroyedByOverload;

    public MinerBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MINER_BE.get(), pos, state);
        this.mineY = pos.getY() - 1;
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MinerBlockEntity miner) {
        miner.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }

        ItemStack drillStack = itemHandler.getStackInSlot(SLOT_DRILL);
        DrillProfile profile = MinerHelper.getDrillProfile(drillStack);
        if (profile == null || itemHandler.getStackInSlot(SLOT_PIPE).isEmpty()) {
            return;
        }

        if (!hasOutputSpace()) {
            return;
        }

        ScannerMode scannerMode = MinerHelper.getScannerMode(itemHandler.getStackInSlot(SLOT_SCANNER));
        int interval = MinerHelper.getMineInterval(profile, scannerMode);

        tickCount++;
        if (tickCount < interval) {
            return;
        }
        tickCount = 0;

        BlockPos shaftCenter = new BlockPos(worldPosition.getX(), mineY, worldPosition.getZ());
        if (!level.isInWorldBounds(shaftCenter) || mineY < level.getMinBuildHeight()) {
            done = true;
            setChanged();
            return;
        }

        List<BlockPos> layer = MinerHelper.getLayerPositions(shaftCenter, scannerMode);
        int minedThisCycle = mineLayer(layer, drillStack, profile, scannerMode);

        if (minedThisCycle == 0) {
            mineY--;
            done = false;
            setChanged();
        }
    }

    private int mineLayer(
            final List<BlockPos> layer,
            final ItemStack drillStack,
            final DrillProfile profile,
            final ScannerMode scannerMode) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        int mined = 0;
        for (BlockPos targetPos : layer) {
            if (!level.isInWorldBounds(targetPos)) {
                continue;
            }
            if (itemHandler.getStackInSlot(SLOT_PIPE).isEmpty()) {
                break;
            }

            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.isAir() || !targetState.getFluidState().isEmpty()) {
                continue;
            }
            if (!MinerHelper.shouldMineBlock(level, targetPos, targetState, scannerMode, profile.maxHardness())) {
                continue;
            }
            if (storedEnergy < EU_PER_BLOCK) {
                break;
            }
            if (!MinerHelper.drainDrillEnergy(drillStack, profile.drillEuPerBlock())) {
                break;
            }

            List<ItemStack> drops = Block.getDrops(
                    targetState, serverLevel, targetPos, level.getBlockEntity(targetPos));
            if (!OutputBufferHelper.canFitAll(itemHandler, SLOT_OUTPUT_START, OUTPUT_SLOTS, drops)) {
                break;
            }

            for (ItemStack drop : drops) {
                ItemStack remaining = OutputBufferHelper.insertRange(
                        itemHandler, SLOT_OUTPUT_START, OUTPUT_SLOTS, drop);
                if (!remaining.isEmpty()) {
                    return mined;
                }
            }

            level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            storedEnergy -= EU_PER_BLOCK;
            consumePipe();
            mined++;
            done = false;
        }

        if (mined > 0) {
            setChanged();
        }
        return mined;
    }

    private void consumePipe() {
        ItemStack pipes = itemHandler.getStackInSlot(SLOT_PIPE);
        if (!pipes.isEmpty()) {
            pipes.shrink(1);
            itemHandler.setStackInSlot(SLOT_PIPE, pipes);
        }
    }

    private boolean hasOutputSpace() {
        for (int slot = SLOT_OUTPUT_START; slot < SLOT_COUNT; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
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
        tag.putInt("MineY", mineY);
        tag.putBoolean("Done", done);
        tag.put("Items", itemHandler.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
        mineY = tag.contains("MineY") ? tag.getInt("MineY") : worldPosition.getY() - 1;
        done = tag.getBoolean("Done");
        if (tag.contains("Items")) {
            itemHandler.deserializeNBT(tag.getCompound("Items"));
        } else if (tag.contains("Output")) {
            itemHandler.deserializeNBT(tag.getCompound("Output"));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.miner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MinerMenu(containerId, playerInventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        if (capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            if (side == null || side == Direction.DOWN) {
                return automationOptional.cast();
            }
            return itemHandlerOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
        itemHandlerOptional.invalidate();
        automationOptional.invalidate();
    }
}
