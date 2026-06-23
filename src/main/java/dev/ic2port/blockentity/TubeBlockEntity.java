package dev.ic2port.blockentity;

import dev.ic2port.block.BaseTubeBlock;
import dev.ic2port.block.ColorFilterTubeBlock;
import dev.ic2port.block.ExtractionTubeBlock;
import dev.ic2port.block.FilteredExtractionTubeBlock;
import dev.ic2port.block.FilterTubeBlock;
import dev.ic2port.block.HoverTubeBlock;
import dev.ic2port.block.InsertionTubeBlock;
import dev.ic2port.block.LimiterTubeBlock;
import dev.ic2port.block.PickupTubeBlock;
import dev.ic2port.block.ProviderTubeBlock;
import dev.ic2port.block.RedstoneTubeBlock;
import dev.ic2port.block.RequestTubeBlock;
import dev.ic2port.block.RoundRobinTubeBlock;
import dev.ic2port.block.SortingTubeBlock;
import dev.ic2port.block.SpeedTubeBlock;
import dev.ic2port.block.StackingTubeBlock;
import dev.ic2port.block.StickyTubeBlock;
import dev.ic2port.block.SwitchTubeBlock;
import dev.ic2port.block.TeleportTubeBlock;
import dev.ic2port.block.TransportTubeBlock;
import dev.ic2port.block.VoidTubeBlock;
import dev.ic2port.menu.FilterTubeMenu;
import dev.ic2port.menu.StackingTubeMenu;
import dev.ic2port.menu.TeleportTubeMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.block.VoidTubeBlock;
import dev.ic2port.tube.TubeLogisticsService;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.tube.TubeRoutingService;
import dev.ic2port.tube.TransportedItem;
import dev.ic2port.tube.handler.ExtractionTubeHandler;
import dev.ic2port.tube.handler.ProviderTubeHandler;
import dev.ic2port.tube.handler.VoidTubeHandler;
import dev.ic2port.util.TickProfiler;
import dev.ic2port.util.TubeConnectionHelper;
import dev.ic2port.util.TubeRoutingHelper;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.util.RubberResinExtractor;
import dev.ic2port.util.TubeLogisticsRegistry;
import dev.ic2port.util.TubeTeleportRegistry;
import dev.ic2port.util.TubeTransferHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Item logistics node with animated in-flight items, dye colors, limiter and sorting support.
 */
public class TubeBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_ITEMS = 4;
    public static final int EXTRACTION_INTERVAL = 8;
    public static final int PICKUP_INTERVAL = 4;
    public static final int LOGISTICS_INTERVAL = 20;
    public static final int PROVIDE_INTERVAL = 8;
    public static final int STICKY_INTERVAL = 12;
    public static final byte SPEED_TUBE_BOOST = 24;
    private static final int[] STACKING_THRESHOLDS = {8, 16, 32, 64};

    private final List<TransportedItem> inFlight = new ArrayList<>();
    private final Map<Direction, ItemStack> sideFilters = new HashMap<>();
    private final Map<Direction, DyeColor> colorRoutes = new HashMap<>();
    private final ItemStackHandler filterHandler = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(final int slot) {
            TubeBlockEntity.this.syncToClient();
        }
    };
    private EnumSet<DyeColor> allowedColors = EnumSet.noneOf(DyeColor.class);

    private int pushPriorityIndex;
    private int extractionCooldown;
    private int pickupCooldown;
    private int logisticsCooldown;
    private int provideCooldown;
    private int stickyCooldown;
    private ItemStack requestFilter = ItemStack.EMPTY;
    private ItemStack extractionFilter = ItemStack.EMPTY;
    private EnumSet<Direction> blockedOutputs = EnumSet.noneOf(Direction.class);
    private boolean redstoneControlled;
    private boolean largePickupRadius;
    private boolean onlyExistingInventories;
    private boolean pulseExtract;
    private boolean comparatorFromInventories;
    private boolean lastRedstonePowered;
    @Nullable
    private Direction extraExtractDirection;
    @Nullable
    private Direction outputPriority;
    @Nullable
    private DyeColor paintColor;

    private ItemStack stackBuffer = ItemStack.EMPTY;
    private int stackingThreshold = 64;
    private int teleportNetworkId;
    private boolean teleportSend = true;
    private boolean teleportReceive = true;

    @Nullable
    private TubeRole cachedRole;

    public TubeBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.TUBE_BE.get(), pos, state);
    }

    @Override
    public void setBlockState(final BlockState state) {
        super.setBlockState(state);
        cachedRole = null;
    }

    public TubeRole getCachedRole() {
        if (cachedRole == null) {
            cachedRole = resolveRole();
        }
        return cachedRole;
    }

    public TubeRole getRole() {
        return getCachedRole();
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final TubeBlockEntity tube) {
        TickProfiler.profileTube(() -> serverTickBody(level, pos, state, tube));
    }

    private static void serverTickBody(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final TubeBlockEntity tube) {
        boolean powered = level.hasNeighborSignal(pos);
        if (tube.pulseExtract && tube.redstoneControlled && tube.supportsExtraction()) {
            if (powered && !tube.lastRedstonePowered && tube.inFlight.size() < MAX_ITEMS) {
                if (ExtractionTubeHandler.INSTANCE.tryExtract(tube, state)) {
                    tube.syncToClient();
                    tube.notifyRedstoneNeighbors(state);
                }
            }
            tube.lastRedstonePowered = powered;
        } else {
            tube.lastRedstonePowered = powered;
        }

        if (tube.isSwitchClosed()) {
            return;
        }

        if (!tube.isTubeActive()) {
            return;
        }
        boolean changed = false;
        for (TransportedItem item : tube.inFlight) {
            item.advance();
            changed = true;
        }

        Iterator<TransportedItem> iterator = tube.inFlight.iterator();
        while (iterator.hasNext()) {
            TransportedItem item = iterator.next();
            if (item.isReady() && tube.tryTransfer(item, state)) {
                iterator.remove();
                changed = true;
            }
        }

        if (tube.supportsExtraction() && !tube.pulseExtract && tube.inFlight.size() < MAX_ITEMS) {
            if (tube.extractionCooldown > 0) {
                tube.extractionCooldown--;
            } else {
                tube.extractionCooldown = tube.getCachedRole() == TubeRole.FILTERED_EXTRACTION
                        ? EXTRACTION_INTERVAL / 2
                        : EXTRACTION_INTERVAL;
                if (ExtractionTubeHandler.INSTANCE.tryExtract(tube, state)) {
                    changed = true;
                }
            }
        }

        if (tube.getRole() == TubeRole.PICKUP && tube.inFlight.size() < MAX_ITEMS) {
            if (tube.pickupCooldown > 0) {
                tube.pickupCooldown--;
            } else {
                tube.pickupCooldown = PICKUP_INTERVAL;
                if (tube.tryPickupItems()) {
                    changed = true;
                }
            }
        }

        if (tube.getRole() == TubeRole.STACKING && tube.tryEjectStackBuffer(state)) {
            changed = true;
        }

        if (tube.getCachedRole() == TubeRole.REQUEST) {
            TubeLogisticsService.tickLogisticRequest(tube);
        }

        if (tube.getCachedRole() == TubeRole.PROVIDER && tube.inFlight.size() < MAX_ITEMS) {
            if (tube.provideCooldown > 0) {
                tube.provideCooldown--;
            } else {
                tube.provideCooldown = PROVIDE_INTERVAL;
                if (ProviderTubeHandler.INSTANCE.tryFulfillLogisticRequest(tube, state)) {
                    changed = true;
                }
            }
        }

        if (tube.getRole() == TubeRole.STICKY && tube.inFlight.size() < MAX_ITEMS) {
            if (tube.stickyCooldown > 0) {
                tube.stickyCooldown--;
            } else {
                tube.stickyCooldown = STICKY_INTERVAL;
                if (tube.tryStickyExtract(state)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            tube.syncToClient();
            tube.notifyRedstoneNeighbors(state);
        }
    }

    private TubeRole resolveRole() {
        if (getBlockState().getBlock() instanceof VoidTubeBlock) {
            return TubeRole.VOID;
        }
        if (getBlockState().getBlock() instanceof FilteredExtractionTubeBlock) {
            return TubeRole.FILTERED_EXTRACTION;
        }
        if (getBlockState().getBlock() instanceof ExtractionTubeBlock) {
            return TubeRole.EXTRACTION;
        }
        if (getBlockState().getBlock() instanceof RequestTubeBlock) {
            return TubeRole.REQUEST;
        }
        if (getBlockState().getBlock() instanceof ProviderTubeBlock) {
            return TubeRole.PROVIDER;
        }
        if (getBlockState().getBlock() instanceof InsertionTubeBlock) {
            return TubeRole.INSERTION;
        }
        if (getBlockState().getBlock() instanceof StickyTubeBlock) {
            return TubeRole.STICKY;
        }
        if (getBlockState().getBlock() instanceof LimiterTubeBlock) {
            return TubeRole.LIMITER;
        }
        if (getBlockState().getBlock() instanceof SortingTubeBlock) {
            return TubeRole.SORTING;
        }
        if (getBlockState().getBlock() instanceof FilterTubeBlock) {
            return TubeRole.FILTER;
        }
        if (getBlockState().getBlock() instanceof ColorFilterTubeBlock) {
            return TubeRole.COLOR_FILTER;
        }
        if (getBlockState().getBlock() instanceof SpeedTubeBlock) {
            return TubeRole.SPEED;
        }
        if (getBlockState().getBlock() instanceof HoverTubeBlock) {
            return TubeRole.HOVER;
        }
        if (getBlockState().getBlock() instanceof PickupTubeBlock) {
            return TubeRole.PICKUP;
        }
        if (getBlockState().getBlock() instanceof RedstoneTubeBlock) {
            return TubeRole.REDSTONE;
        }
        if (getBlockState().getBlock() instanceof SwitchTubeBlock) {
            return TubeRole.SWITCH;
        }
        if (getBlockState().getBlock() instanceof TransportTubeBlock) {
            return TubeRole.TRANSPORT_ONLY;
        }
        if (getBlockState().getBlock() instanceof RoundRobinTubeBlock) {
            return TubeRole.ROUND_ROBIN;
        }
        if (getBlockState().getBlock() instanceof StackingTubeBlock) {
            return TubeRole.STACKING;
        }
        if (getBlockState().getBlock() instanceof TeleportTubeBlock) {
            return TubeRole.TELEPORT;
        }
        if (getBlockState().getBlock() instanceof VoidTubeBlock) {
            return TubeRole.VOID;
        }
        return TubeRole.TRANSPORT;
    }

    public boolean isSwitchClosed() {
        return getRole() == TubeRole.SWITCH && level != null && level.hasNeighborSignal(worldPosition);
    }

    public boolean supportsExtraction() {
        TubeRole role = getRole();
        return role == TubeRole.EXTRACTION || role == TubeRole.FILTERED_EXTRACTION;
    }

    public boolean isTubeActive() {
        return !redstoneControlled || level == null || !level.hasNeighborSignal(worldPosition);
    }

    public int getPushPriorityIndex() {
        return pushPriorityIndex;
    }

    public Map<Direction, ItemStack> getSideFilters() {
        return sideFilters;
    }

    public Map<Direction, DyeColor> getColorRoutes() {
        return colorRoutes;
    }

    public ItemStack getExtractionFilter() {
        return extractionFilter;
    }

    public ItemStack getRequestFilter() {
        return requestFilter;
    }

    @Nullable
    public Direction getExtraExtractDirection() {
        return extraExtractDirection;
    }

    /**
     * @return {@code true} while the logistics tick is cooling down
     */
    public boolean decrementLogisticsCooldown() {
        if (logisticsCooldown > 0) {
            logisticsCooldown--;
            return true;
        }
        logisticsCooldown = LOGISTICS_INTERVAL;
        return false;
    }

    public void setExtraExtractDirection(@Nullable final Direction direction) {
        extraExtractDirection = direction;
        syncToClient();
    }

    public boolean toggleBlockedOutput(final Direction direction) {
        boolean blocked;
        if (blockedOutputs.contains(direction)) {
            blockedOutputs.remove(direction);
            blocked = false;
        } else {
            blockedOutputs.add(direction);
            blocked = true;
        }
        syncToClient();
        return blocked;
    }

    public boolean toggleRedstoneControl() {
        redstoneControlled = !redstoneControlled;
        syncToClient();
        return redstoneControlled;
    }

    public void setExtractionFilter(final ItemStack filter) {
        extractionFilter = filter.copyWithCount(1);
        syncToClient();
    }

    public void clearExtractionFilter() {
        extractionFilter = ItemStack.EMPTY;
        syncToClient();
    }

    public void setOutputPriority(@Nullable final Direction direction) {
        outputPriority = direction;
        syncToClient();
    }

    @Nullable
    public Direction getOutputPriority() {
        return outputPriority;
    }

    public boolean tryExtract(final BlockState state) {
        return ExtractionTubeHandler.INSTANCE.tryExtract(this, state);
    }

    public boolean toggleLargePickupRadius() {
        largePickupRadius = !largePickupRadius;
        syncToClient();
        return largePickupRadius;
    }

    public boolean toggleOnlyExistingInventories() {
        onlyExistingInventories = !onlyExistingInventories;
        syncToClient();
        return onlyExistingInventories;
    }

    public boolean togglePulseExtract() {
        pulseExtract = !pulseExtract;
        syncToClient();
        return pulseExtract;
    }

    public boolean toggleComparatorFromInventories() {
        comparatorFromInventories = !comparatorFromInventories;
        syncToClient();
        notifyRedstoneNeighbors(getBlockState());
        return comparatorFromInventories;
    }

    public int getRedstoneTubeSignal() {
        return getRole() == TubeRole.REDSTONE && !inFlight.isEmpty() ? 15 : 0;
    }

    public int getComparatorOutput() {
        if (comparatorFromInventories) {
            return computeInventoryComparator();
        }
        if (getRole() == TubeRole.STACKING && !stackBuffer.isEmpty()) {
            return Mth.floor((stackBuffer.getCount() / (float) stackingThreshold) * 15.0F);
        }
        return Mth.floor((inFlight.size() / (float) MAX_ITEMS) * 15.0F);
    }

    public int getStackBufferCount() {
        return stackBuffer.getCount();
    }

    public int getStackingThreshold() {
        return stackingThreshold;
    }

    public void cycleStackingThreshold() {
        for (int index = 0; index < STACKING_THRESHOLDS.length; index++) {
            if (STACKING_THRESHOLDS[index] == stackingThreshold) {
                stackingThreshold = STACKING_THRESHOLDS[(index + 1) % STACKING_THRESHOLDS.length];
                syncToClient();
                return;
            }
        }
        stackingThreshold = STACKING_THRESHOLDS[0];
        syncToClient();
    }

    public void forceEjectStackBuffer() {
        if (level == null || level.isClientSide || stackBuffer.isEmpty()) {
            return;
        }
        tryEjectStackBuffer(getBlockState(), true);
    }

    public int getTeleportNetworkId() {
        return teleportNetworkId;
    }

    public boolean isTeleportSend() {
        return teleportSend;
    }

    public boolean isTeleportReceive() {
        return teleportReceive;
    }

    public void cycleTeleportNetworkId() {
        adjustTeleportNetworkId(1);
    }

    public void adjustTeleportNetworkId(final int delta) {
        if (level != null && !level.isClientSide && getRole() == TubeRole.TELEPORT) {
            TubeTeleportRegistry.unregister(this);
        }
        teleportNetworkId = Math.floorMod(teleportNetworkId + delta, 16);
        syncToClient();
        refreshTeleportRegistration();
    }

    public void toggleTeleportSend() {
        teleportSend = !teleportSend;
        syncToClient();
    }

    public void toggleTeleportReceive() {
        if (level != null && !level.isClientSide) {
            TubeTeleportRegistry.unregister(this);
        }
        teleportReceive = !teleportReceive;
        syncToClient();
        refreshTeleportRegistration();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshTeleportRegistration();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            if (getRole() == TubeRole.TELEPORT) {
                TubeTeleportRegistry.unregister(this);
            }
            if (getRole() == TubeRole.REQUEST) {
                TubeLogisticsRegistry.clearRequester(this);
            }
        }
        super.setRemoved();
    }

    private void refreshTeleportRegistration() {
        if (level == null || level.isClientSide || getRole() != TubeRole.TELEPORT) {
            return;
        }
        if (teleportReceive) {
            TubeTeleportRegistry.register(this);
        }
    }

    public ItemStackHandler getFilterHandler() {
        return filterHandler;
    }

    public void syncToClientPublic() {
        syncToClient();
    }

    @Override
    public Component getDisplayName() {
        return switch (getRole()) {
            case FILTER -> Component.translatable("block.ic2port.filter_tube");
            case STACKING -> Component.translatable("block.ic2port.stacking_tube");
            case TELEPORT -> Component.translatable("block.ic2port.teleport_tube");
            default -> Component.translatable("block.ic2port.item_tube");
        };
    }

    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return switch (getRole()) {
            case FILTER -> new FilterTubeMenu(containerId, playerInventory, this);
            case STACKING -> new StackingTubeMenu(containerId, playerInventory, this);
            case TELEPORT -> new TeleportTubeMenu(containerId, playerInventory, this);
            default -> null;
        };
    }

    public void setColorRoute(final Direction side, final DyeColor color) {
        colorRoutes.put(side, color);
        syncToClient();
    }

    public void clearColorRoute(final Direction side) {
        colorRoutes.remove(side);
        syncToClient();
    }

    public List<TransportedItem> getInFlightItems() {
        return inFlight;
    }

    @Nullable
    public DyeColor getPaintColor() {
        return paintColor;
    }

    public void setPaintColor(@Nullable final DyeColor paintColor) {
        this.paintColor = paintColor;
        syncToClient();
    }

    public void toggleAllowedColor(final DyeColor color) {
        if (allowedColors.contains(color)) {
            allowedColors.remove(color);
        } else {
            allowedColors.add(color);
        }
        syncToClient();
    }

    public void clearAllowedColors() {
        allowedColors.clear();
        syncToClient();
    }

    public void setSideFilter(final Direction side, final ItemStack filter) {
        if (filter.isEmpty()) {
            sideFilters.remove(side);
        } else {
            sideFilters.put(side, filter.copyWithCount(1));
        }
        syncToClient();
    }

    public void clearSideFilter(final Direction side) {
        sideFilters.remove(side);
        syncToClient();
    }

    public void setRequestFilter(final ItemStack filter) {
        requestFilter = filter.copyWithCount(1);
        syncToClient();
    }

    public void clearRequestFilter() {
        requestFilter = ItemStack.EMPTY;
        syncToClient();
    }

    @Nullable
    public Direction getInventoryFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(dev.ic2port.block.DirectionalTubeBlock.FACING)) {
            return state.getValue(dev.ic2port.block.DirectionalTubeBlock.FACING);
        }
        return null;
    }

    public boolean canAcceptFrom(final ItemStack stack, final Direction from) {
        if (stack.isEmpty() || isSwitchClosed()) {
            return false;
        }
        if (getRole() == TubeRole.TELEPORT && !teleportReceive) {
            return false;
        }
        if (getRole() == TubeRole.REQUEST) {
            return canDeliverToInventory(stack);
        }
        if (getRole() == TubeRole.STACKING) {
            return canAbsorbIntoStackBuffer(stack);
        }
        if (getRole() == TubeRole.VOID) {
            return true;
        }
        if (inFlight.size() >= MAX_ITEMS) {
            return false;
        }
        return true;
    }

    public boolean acceptFromNetwork(final ItemStack stack, final Direction from) {
        if (getCachedRole() == TubeRole.VOID) {
            return VoidTubeHandler.INSTANCE.acceptFromNetwork(this, getBlockState());
        }
        if (!canAcceptFrom(stack, from)) {
            return false;
        }
        if (getRole() == TubeRole.REQUEST) {
            ItemStack remainder = deliverToInventory(stack);
            if (remainder.isEmpty()) {
                syncToClient();
                return true;
            }
            return false;
        }
        if (getRole() == TubeRole.STACKING) {
            if (absorbIntoStackBuffer(stack)) {
                syncToClient();
                return true;
            }
            return false;
        }
        if (getRole() == TubeRole.VOID) {
            syncToClient();
            return true;
        }

        TransportedItem transported = new TransportedItem(stack, from);
        TubeRoutingHelper.applyTubePaint(transported, paintColor);
        if (getRole() == TubeRole.LIMITER && !TubeRoutingHelper.passesLimiter(allowedColors, transported)) {
            return false;
        }
        if (getRole() == TubeRole.SPEED) {
            transported.setSpeed(SPEED_TUBE_BOOST);
        }
        if (getRole() == TubeRole.HOVER) {
            transported.setHovering(true);
        }
        inFlight.add(transported);
        syncToClient();
        return true;
    }

    private boolean tryTransfer(final TransportedItem item, final BlockState state) {
        if (getRole() == TubeRole.VOID) {
            return true;
        }
        if (getRole() == TubeRole.TELEPORT && teleportSend) {
            if (tryTeleportTransfer(item)) {
                return true;
            }
            item.resetProgress();
            return false;
        }
        List<Direction> directions = TubeRoutingService.resolveDirections(this, item, state);
        for (Direction direction : directions) {
            if (!BaseTubeBlock.isConnected(state, direction)) {
                continue;
            }
            if (item.getEntryDirection() != null && direction == item.getEntryDirection()) {
                continue;
            }
            if (blockedOutputs.contains(direction)) {
                continue;
            }
            BlockPos neighborPos = worldPosition.relative(direction);
            TubeBlockEntity neighborTube = TubeConnectionHelper.getTube(level, neighborPos);
            if (neighborTube != null) {
                if (neighborTube.acceptFromNetwork(item.getStack(), direction.getOpposite())) {
                    pushPriorityIndex = (direction.get3DDataValue() + 1) % 6;
                    return true;
                }
                continue;
            }
            IItemHandler handler = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
            if (onlyExistingInventories
                    && !TubeTransferHelper.handlerContainsMatchingItem(handler, item.getStack())) {
                continue;
            }
            ItemStack remainder = TubeTransferHelper.insertIntoHandler(handler, item.getStack());
            if (remainder.getCount() != item.getStack().getCount()) {
                pushPriorityIndex = (direction.get3DDataValue() + 1) % 6;
                return true;
            }
        }
        item.resetProgress();
        return false;
    }

    private boolean tryTeleportTransfer(final TransportedItem item) {
        if (level == null) {
            return false;
        }
        TubeBlockEntity destination = TubeTeleportRegistry.findReceiver(level, teleportNetworkId, worldPosition);
        if (destination == null) {
            return false;
        }
        return destination.acceptFromNetwork(item.getStack(), null);
    }

    private boolean canAbsorbIntoStackBuffer(final ItemStack stack) {
        if (stackBuffer.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(stackBuffer, stack)) {
            return false;
        }
        return stackBuffer.getCount() < stackBuffer.getMaxStackSize();
    }

    private boolean absorbIntoStackBuffer(final ItemStack stack) {
        if (!canAbsorbIntoStackBuffer(stack)) {
            return false;
        }
        if (stackBuffer.isEmpty()) {
            stackBuffer = stack.copyWithCount(1);
        } else {
            stackBuffer.grow(1);
        }
        return true;
    }

    private boolean tryEjectStackBuffer(final BlockState state) {
        return tryEjectStackBuffer(state, false);
    }

    private boolean tryEjectStackBuffer(final BlockState state, final boolean force) {
        if (stackBuffer.isEmpty()) {
            return false;
        }
        if (!force && stackBuffer.getCount() < stackingThreshold) {
            return false;
        }
        Direction facing = getInventoryFacing();
        if (facing == null || !BaseTubeBlock.isConnected(state, facing)) {
            return false;
        }
        int count = force ? stackBuffer.getCount() : Math.min(stackBuffer.getCount(), stackingThreshold);
        ItemStack outgoing = stackBuffer.copyWithCount(count);
        TransportedItem transported = new TransportedItem(outgoing, facing);
        transported.setExportDirection(facing);
        TubeRoutingHelper.applyTubePaint(transported, paintColor);
        if (pushItemOut(transported, state, facing)) {
            stackBuffer.shrink(outgoing.getCount());
            if (stackBuffer.isEmpty()) {
                stackBuffer = ItemStack.EMPTY;
            }
            return true;
        }
        return false;
    }

    private boolean pushItemOut(
            final TransportedItem item,
            final BlockState state,
            final Direction direction) {
        if (!BaseTubeBlock.isConnected(state, direction) || blockedOutputs.contains(direction)) {
            return false;
        }
        BlockPos neighborPos = worldPosition.relative(direction);
        TubeBlockEntity neighborTube = TubeConnectionHelper.getTube(level, neighborPos);
        if (neighborTube != null) {
            return neighborTube.acceptFromNetwork(item.getStack(), direction.getOpposite());
        }
        IItemHandler handler = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
        if (onlyExistingInventories
                && !TubeTransferHelper.handlerContainsMatchingItem(handler, item.getStack())) {
            return false;
        }
        ItemStack remainder = TubeTransferHelper.insertIntoHandler(handler, item.getStack());
        return remainder.getCount() != item.getStack().getCount();
    }

    private boolean tryStickyExtract(final BlockState state) {
        if (level == null) {
            return false;
        }
        ItemStack resinFilter = new ItemStack(ItemRegistry.STICKY_RESIN.get());
        for (Direction direction : Direction.values()) {
            if (!BaseTubeBlock.isConnected(state, direction)) {
                continue;
            }
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.is(BlockRegistry.RUBBER_WOOD.get())) {
                ItemStack resin = RubberResinExtractor.extractForAutomation(level, neighborPos, neighborState);
                if (!resin.isEmpty()) {
                    TransportedItem transported = new TransportedItem(resin, direction);
                    TubeRoutingHelper.applyTubePaint(transported, paintColor);
                    inFlight.add(transported);
                    return true;
                }
            }
            IItemHandler handler = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
            ItemStack extracted = TubeTransferHelper.extractMatchingItem(handler, resinFilter);
            if (!extracted.isEmpty()) {
                TransportedItem transported = new TransportedItem(extracted, direction);
                TubeRoutingHelper.applyTubePaint(transported, paintColor);
                inFlight.add(transported);
                return true;
            }
        }
        return false;
    }

    private boolean tryPickupItems() {
        if (level == null) {
            return false;
        }
        double radius = largePickupRadius ? 2.5D : 1.5D;
        AABB area = new AABB(worldPosition).inflate(radius);
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, area, entity -> !entity.getItem().isEmpty());
        for (ItemEntity entity : entities) {
            if (inFlight.size() >= MAX_ITEMS) {
                break;
            }
            ItemStack stack = entity.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack picked = stack.split(1);
            if (stack.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(stack);
            }
            TransportedItem transported = new TransportedItem(picked, Direction.DOWN);
            TubeRoutingHelper.applyTubePaint(transported, paintColor);
            inFlight.add(transported);
            return true;
        }
        return false;
    }

    private int computeInventoryComparator() {
        if (level == null) {
            return 0;
        }
        BlockState state = getBlockState();
        Direction inventoryFacing = getInventoryFacing();
        int best = 0;
        for (Direction direction : Direction.values()) {
            if (inventoryFacing != null && direction == inventoryFacing) {
                continue;
            }
            if (!BaseTubeBlock.isConnected(state, direction)) {
                continue;
            }
            BlockPos neighborPos = worldPosition.relative(direction);
            IItemHandler handler = TubeConnectionHelper.getItemHandler(level, neighborPos, direction.getOpposite());
            best = Math.max(best, TubeTransferHelper.computeFillRatio(handler));
        }
        return best;
    }

    private void notifyRedstoneNeighbors(final BlockState state) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (getRole() == TubeRole.REDSTONE || comparatorFromInventories) {
            level.updateNeighborsAt(worldPosition, state.getBlock());
        }
    }

    private boolean canDeliverToInventory(final ItemStack stack) {
        if (!TubeTransferHelper.matchesFilter(requestFilter, stack)) {
            return false;
        }
        Direction facing = getInventoryFacing();
        if (facing == null) {
            return false;
        }
        BlockPos inventoryPos = worldPosition.relative(facing);
        IItemHandler handler = TubeConnectionHelper.getItemHandler(level, inventoryPos, facing.getOpposite());
        if (handler == null) {
            return false;
        }
        ItemStack remainder = TubeTransferHelper.insertIntoHandler(handler, stack.copyWithCount(1));
        return remainder.isEmpty();
    }

    private ItemStack deliverToInventory(final ItemStack stack) {
        Direction facing = getInventoryFacing();
        if (facing == null) {
            return stack;
        }
        BlockPos inventoryPos = worldPosition.relative(facing);
        IItemHandler handler = TubeConnectionHelper.getItemHandler(level, inventoryPos, facing.getOpposite());
        return TubeTransferHelper.insertIntoHandler(handler, stack);
    }

    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag items = new ListTag();
        for (TransportedItem item : inFlight) {
            items.add(item.save());
        }
        tag.put("InFlight", items);
        tag.putInt("PushPriority", pushPriorityIndex);
        if (!requestFilter.isEmpty()) {
            tag.put("RequestFilter", requestFilter.save(new CompoundTag()));
        }
        if (!extractionFilter.isEmpty()) {
            tag.put("ExtractionFilter", extractionFilter.save(new CompoundTag()));
        }
        if (!blockedOutputs.isEmpty()) {
            int mask = 0;
            for (Direction direction : blockedOutputs) {
                mask |= 1 << direction.get3DDataValue();
            }
            tag.putInt("BlockedOutputs", mask);
        }
        tag.putBoolean("RedstoneControlled", redstoneControlled);
        tag.putBoolean("LargePickupRadius", largePickupRadius);
        tag.putBoolean("OnlyExistingInventories", onlyExistingInventories);
        tag.putBoolean("PulseExtract", pulseExtract);
        tag.putBoolean("ComparatorFromInventories", comparatorFromInventories);
        if (extraExtractDirection != null) {
            tag.putInt("ExtraExtractDirection", extraExtractDirection.get3DDataValue());
        }
        if (outputPriority != null) {
            tag.putInt("OutputPriority", outputPriority.get3DDataValue());
        }
        if (paintColor != null) {
            tag.putInt("PaintColor", paintColor.getId());
        }
        if (!allowedColors.isEmpty()) {
            int mask = 0;
            for (DyeColor color : allowedColors) {
                mask |= 1 << color.getId();
            }
            tag.putInt("AllowedColors", mask);
        }
        if (!sideFilters.isEmpty()) {
            CompoundTag filters = new CompoundTag();
            for (Map.Entry<Direction, ItemStack> entry : sideFilters.entrySet()) {
                filters.put(String.valueOf(entry.getKey().get3DDataValue()), entry.getValue().save(new CompoundTag()));
            }
            tag.put("SideFilters", filters);
        }
        tag.put("FilterSlots", filterHandler.serializeNBT());
        if (!colorRoutes.isEmpty()) {
            CompoundTag routes = new CompoundTag();
            for (Map.Entry<Direction, DyeColor> entry : colorRoutes.entrySet()) {
                routes.putInt(String.valueOf(entry.getKey().get3DDataValue()), entry.getValue().getId());
            }
            tag.put("ColorRoutes", routes);
        }
        if (!stackBuffer.isEmpty()) {
            tag.put("StackBuffer", stackBuffer.save(new CompoundTag()));
        }
        tag.putInt("StackingThreshold", stackingThreshold);
        tag.putInt("TeleportNetworkId", teleportNetworkId);
        tag.putBoolean("TeleportSend", teleportSend);
        tag.putBoolean("TeleportReceive", teleportReceive);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        inFlight.clear();
        if (tag.contains("InFlight", Tag.TAG_LIST)) {
            ListTag items = tag.getList("InFlight", Tag.TAG_COMPOUND);
            for (Tag entry : items) {
                TransportedItem item = TransportedItem.load((CompoundTag) entry);
                if (item != null) {
                    inFlight.add(item);
                }
            }
        } else if (tag.contains("HeldStack")) {
            TransportedItem legacy = new TransportedItem(
                    ItemStack.of(tag.getCompound("HeldStack")),
                    tag.contains("EntryDirection")
                            ? Direction.from3DDataValue(tag.getInt("EntryDirection"))
                            : null);
            inFlight.add(legacy);
        }
        pushPriorityIndex = tag.getInt("PushPriority");
        requestFilter = tag.contains("RequestFilter")
                ? ItemStack.of(tag.getCompound("RequestFilter"))
                : ItemStack.EMPTY;
        extractionFilter = tag.contains("ExtractionFilter")
                ? ItemStack.of(tag.getCompound("ExtractionFilter"))
                : ItemStack.EMPTY;
        blockedOutputs.clear();
        if (tag.contains("BlockedOutputs")) {
            int mask = tag.getInt("BlockedOutputs");
            for (Direction direction : Direction.values()) {
                if ((mask & (1 << direction.get3DDataValue())) != 0) {
                    blockedOutputs.add(direction);
                }
            }
        }
        redstoneControlled = tag.getBoolean("RedstoneControlled");
        largePickupRadius = tag.getBoolean("LargePickupRadius");
        onlyExistingInventories = tag.getBoolean("OnlyExistingInventories");
        pulseExtract = tag.getBoolean("PulseExtract");
        comparatorFromInventories = tag.getBoolean("ComparatorFromInventories");
        extraExtractDirection = tag.contains("ExtraExtractDirection")
                ? Direction.from3DDataValue(tag.getInt("ExtraExtractDirection"))
                : null;
        outputPriority = tag.contains("OutputPriority")
                ? Direction.from3DDataValue(tag.getInt("OutputPriority"))
                : null;
        paintColor = tag.contains("PaintColor") ? DyeColor.byId(tag.getInt("PaintColor")) : null;
        allowedColors.clear();
        if (tag.contains("AllowedColors")) {
            int mask = tag.getInt("AllowedColors");
            for (DyeColor color : DyeColor.values()) {
                if ((mask & (1 << color.getId())) != 0) {
                    allowedColors.add(color);
                }
            }
        }
        sideFilters.clear();
        if (tag.contains("SideFilters")) {
            CompoundTag filters = tag.getCompound("SideFilters");
            for (String key : filters.getAllKeys()) {
                sideFilters.put(
                        Direction.from3DDataValue(Integer.parseInt(key)),
                        ItemStack.of(filters.getCompound(key)));
            }
        }
        if (tag.contains("FilterSlots")) {
            filterHandler.deserializeNBT(tag.getCompound("FilterSlots"));
        }
        colorRoutes.clear();
        if (tag.contains("ColorRoutes")) {
            CompoundTag routes = tag.getCompound("ColorRoutes");
            for (String key : routes.getAllKeys()) {
                colorRoutes.put(Direction.from3DDataValue(Integer.parseInt(key)), DyeColor.byId(routes.getInt(key)));
            }
        }
        stackBuffer = tag.contains("StackBuffer")
                ? ItemStack.of(tag.getCompound("StackBuffer"))
                : ItemStack.EMPTY;
        stackingThreshold = tag.contains("StackingThreshold") ? tag.getInt("StackingThreshold") : 64;
        teleportNetworkId = tag.getInt("TeleportNetworkId");
        teleportSend = !tag.contains("TeleportSend") || tag.getBoolean("TeleportSend");
        teleportReceive = !tag.contains("TeleportReceive") || tag.getBoolean("TeleportReceive");
    }
}
