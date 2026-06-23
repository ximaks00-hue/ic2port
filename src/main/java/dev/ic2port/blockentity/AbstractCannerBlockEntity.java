package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.item.ElectricFoamSprayerItem;
import dev.ic2port.item.ElectricItem;
import dev.ic2port.item.FluidCellItem;
import dev.ic2port.item.ReBatteryItem;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.item.IUpgradeItem;
import dev.ic2port.util.CannerOperationHelper;
import dev.ic2port.util.FoodCanningHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared canner processing logic for LV and vacuum variants.
 */
public abstract class AbstractCannerBlockEntity extends BaseMachineBlockEntity {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_SUPPLY = 1;
    public static final int SLOT_COUNT = 2;

    protected int progress;
    protected int maxProgress;
    private CannerOperationHelper.Operation activeOperation = CannerOperationHelper.Operation.NONE;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> (int) Math.round(getStoredEnergy());
                case 3 -> (int) Math.round(getCapacity());
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

    protected AbstractCannerBlockEntity(
            final BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state,
            final double energyCapacity) {
        super(type, pos, state, SLOT_COUNT, energyCapacity);
        this.maxProgress = getBaseProcessTime();
    }

    protected abstract double getEnergyPerTick();

    protected abstract int getBaseProcessTime();

    protected abstract Component getMenuTitle();

    protected boolean isVacuumCanner() {
        return false;
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final AbstractCannerBlockEntity canner) {
        canner.tickServer();
    }

    @Override
    protected boolean isProcessSlotInput(final int processSlot) {
        return processSlot == SLOT_TOOL || processSlot == SLOT_SUPPLY;
    }

    @Override
    protected boolean isProcessSlotLocked(final int processSlot) {
        return progress > 0 && isProcessSlotInput(processSlot);
    }

    @Override
    protected boolean isValidProcessInput(final int processSlot, final ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (stack.is(ItemRegistry.TIN_CAN.get()) || stack.is(ItemRegistry.FILLED_TIN_CAN.get())) {
            return true;
        }
        if (stack.is(ItemRegistry.TIN_PLATE.get()) && isVacuumCanner()) {
            return true;
        }
        if (FoodCanningHelper.isFoodInput(stack)) {
            return true;
        }
        if (processSlot == SLOT_TOOL) {
            return stack.getItem() instanceof ElectricFoamSprayerItem
                    || stack.getItem() instanceof ReBatteryItem
                    || stack.is(ItemRegistry.HYDRATION_CELL.get());
        }
        if (stack.is(ItemRegistry.FLUID_CELL.get()) || stack.is(Items.BUCKET)
                || stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET)) {
            return true;
        }
        if (processSlot == SLOT_SUPPLY) {
            return stack.is(ItemRegistry.FOAM_PELLET.get())
                    || stack.getItem() instanceof ElectricItem;
        }
        return false;
    }

    private void tickServer() {
        if (!isServerProcessingEnabled()) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            progress = 0;
        }

        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY);
        CannerOperationHelper.Operation operation = CannerOperationHelper.detect(slot0, slot1, isVacuumCanner());
        if (operation == CannerOperationHelper.Operation.NONE) {
            progress = 0;
            activeOperation = CannerOperationHelper.Operation.NONE;
            setChanged();
            return;
        }

        if (operation != activeOperation) {
            progress = 0;
            activeOperation = operation;
        }

        maxProgress = getScaledProcessTime(getBaseProcessTime());
        if (!canStillCompleteOperation(operation, slot0, slot1)) {
            if (CannerOperationHelper.detect(slot0, slot1, isVacuumCanner())
                    == CannerOperationHelper.Operation.NONE) {
                progress = 0;
                activeOperation = CannerOperationHelper.Operation.NONE;
            }
            setChanged();
            return;
        }
        if (!canAdvanceOperation(operation, slot0, slot1)) {
            setChanged();
            return;
        }
        if (!consumeEnergy(getEnergyPerTick())) {
            return;
        }

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        if (!finishOperation(activeOperation)) {
            progress = maxProgress;
        } else {
            progress = 0;
        }
        setChanged();
    }

    private boolean finishOperation(final CannerOperationHelper.Operation operation) {
        return switch (operation) {
            case FOOD_CAN -> finishFoodCan();
            case TIN_CAN_PRESS -> finishTinCanPress();
            case CELL_FILL -> finishCellFill();
            case CELL_EMPTY -> finishCellEmpty();
            default -> finishDefaultOperation(operation);
        };
    }

    private boolean finishDefaultOperation(final CannerOperationHelper.Operation operation) {
        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL).copy();
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY).copy();
        ItemStack beforeTool = getItemHandler().getStackInSlot(SLOT_TOOL).copy();
        ItemStack beforeSupply = getItemHandler().getStackInSlot(SLOT_SUPPLY).copy();
        CannerOperationHelper.finishProcess(slot0, slot1, operation);
        getItemHandler().setStackInSlot(SLOT_TOOL, slot0);
        getItemHandler().setStackInSlot(SLOT_SUPPLY, slot1);
        if (operation == CannerOperationHelper.Operation.HYDRATION_REFILL) {
            if (getItemHandler().getStackInSlot(SLOT_TOOL).is(ItemRegistry.HYDRATION_CELL.get())
                    && getItemHandler().getStackInSlot(SLOT_SUPPLY).isEmpty()) {
                getItemHandler().setStackInSlot(SLOT_SUPPLY, new ItemStack(Items.BUCKET));
            } else if (getItemHandler().getStackInSlot(SLOT_SUPPLY).is(ItemRegistry.HYDRATION_CELL.get())
                    && getItemHandler().getStackInSlot(SLOT_TOOL).isEmpty()) {
                getItemHandler().setStackInSlot(SLOT_TOOL, new ItemStack(Items.BUCKET));
            }
        }
        return !stacksEqual(beforeTool, getItemHandler().getStackInSlot(SLOT_TOOL))
                || !stacksEqual(beforeSupply, getItemHandler().getStackInSlot(SLOT_SUPPLY));
    }

    private static boolean stacksEqual(final ItemStack left, final ItemStack right) {
        return ItemStack.isSameItemSameTags(left, right) && left.getCount() == right.getCount();
    }

    private boolean canStillCompleteOperation(
            final CannerOperationHelper.Operation operation,
            final ItemStack slot0,
            final ItemStack slot1) {
        return CannerOperationHelper.detect(slot0, slot1, isVacuumCanner()) == operation;
    }

    private boolean canAdvanceOperation(
            final CannerOperationHelper.Operation operation,
            final ItemStack slot0,
            final ItemStack slot1) {
        if (operation == CannerOperationHelper.Operation.TIN_CAN_PRESS) {
            return canFitTinCanOutput();
        }
        return CannerOperationHelper.canAdvance(operation, slot0, slot1);
    }

    private boolean canFitTinCanOutput() {
        for (int slot = SLOT_TOOL; slot <= SLOT_SUPPLY; slot++) {
            ItemStack existing = getItemHandler().getStackInSlot(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (existing.is(ItemRegistry.TIN_CAN.get()) && existing.getCount() < existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private boolean finishFoodCan() {
        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY);
        FoodCanningHelper.FoodCanLayout layout = FoodCanningHelper.detectLayout(slot0, slot1);
        if (layout == null || !FoodCanningHelper.canProcessLayout(layout)) {
            return false;
        }
        ItemStack food = getItemHandler().getStackInSlot(layout.foodSlot());
        ItemStack tins = getItemHandler().getStackInSlot(layout.tinSlot());
        ItemStack filled = FoodCanningHelper.createFilledCan(food);

        boolean deposited = false;
        if (tins.is(ItemRegistry.TIN_CAN.get())) {
            getItemHandler().setStackInSlot(layout.tinSlot(), filled);
            deposited = true;
        } else if (tins.is(ItemRegistry.FILLED_TIN_CAN.get())
                && ItemStack.isSameItemSameTags(tins, filled)
                && tins.getCount() < tins.getMaxStackSize()) {
            tins.grow(1);
            getItemHandler().setStackInSlot(layout.tinSlot(), tins);
            deposited = true;
        }

        if (deposited) {
            FoodCanningHelper.consumeOnePoint(food);
            getItemHandler().setStackInSlot(layout.foodSlot(), food);
        }
        return deposited;
    }

    private boolean finishTinCanPress() {
        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL).copy();
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY).copy();
        CannerOperationHelper.finishProcess(slot0, slot1, CannerOperationHelper.Operation.TIN_CAN_PRESS);
        getItemHandler().setStackInSlot(SLOT_TOOL, slot0);
        getItemHandler().setStackInSlot(SLOT_SUPPLY, slot1);
        ItemStack tinCan = new ItemStack(ItemRegistry.TIN_CAN.get());
        if (tryInsertTinCan(SLOT_TOOL, tinCan) || tryInsertTinCan(SLOT_SUPPLY, tinCan)) {
            return true;
        }
        if (level != null) {
            Block.popResource(level, worldPosition, tinCan);
        }
        return true;
    }

    private boolean finishCellFill() {
        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY);
        final int cellSlot;
        final int bucketSlot;
        if (slot0.is(ItemRegistry.FLUID_CELL.get()) && FluidCellItem.isEmpty(slot0)
                && (slot1.is(Items.WATER_BUCKET) || slot1.is(Items.LAVA_BUCKET))) {
            cellSlot = SLOT_TOOL;
            bucketSlot = SLOT_SUPPLY;
        } else if (slot1.is(ItemRegistry.FLUID_CELL.get()) && FluidCellItem.isEmpty(slot1)
                && (slot0.is(Items.WATER_BUCKET) || slot0.is(Items.LAVA_BUCKET))) {
            cellSlot = SLOT_SUPPLY;
            bucketSlot = SLOT_TOOL;
        } else {
            return false;
        }
        ItemStack bucketStack = getItemHandler().getStackInSlot(bucketSlot);
        var fluid = bucketStack.is(Items.WATER_BUCKET) ? Fluids.WATER : Fluids.LAVA;
        ItemStack filledCell = FluidCellItem.fillCell(getItemHandler().getStackInSlot(cellSlot), fluid);
        getItemHandler().setStackInSlot(cellSlot, filledCell);
        getItemHandler().setStackInSlot(bucketSlot, new ItemStack(Items.BUCKET));
        return true;
    }

    private boolean finishCellEmpty() {
        ItemStack slot0 = getItemHandler().getStackInSlot(SLOT_TOOL);
        ItemStack slot1 = getItemHandler().getStackInSlot(SLOT_SUPPLY);
        final int cellSlot;
        final int bucketSlot;
        if (slot0.is(ItemRegistry.FLUID_CELL.get()) && !FluidCellItem.isEmpty(slot0)
                && slot1.is(Items.BUCKET)) {
            cellSlot = SLOT_TOOL;
            bucketSlot = SLOT_SUPPLY;
        } else if (slot1.is(ItemRegistry.FLUID_CELL.get()) && !FluidCellItem.isEmpty(slot1)
                && slot0.is(Items.BUCKET)) {
            cellSlot = SLOT_SUPPLY;
            bucketSlot = SLOT_TOOL;
        } else {
            return false;
        }
        ItemStack cellStack = getItemHandler().getStackInSlot(cellSlot);
        var fluid = FluidCellItem.getFluid(cellStack);
        if (fluid == null) {
            return false;
        }
        net.minecraft.world.item.Item bucketItem;
        if (fluid == Fluids.LAVA) {
            bucketItem = Items.LAVA_BUCKET;
        } else if (fluid == Fluids.WATER) {
            bucketItem = Items.WATER_BUCKET;
        } else {
            return false;
        }
        getItemHandler().setStackInSlot(cellSlot, FluidCellItem.emptyCell(ItemRegistry.FLUID_CELL.get()));
        getItemHandler().setStackInSlot(bucketSlot, new ItemStack(bucketItem));
        return true;
    }

    private boolean tryInsertTinCan(final int slot, final ItemStack tinCan) {
        ItemStack existing = getItemHandler().getStackInSlot(slot);
        if (existing.isEmpty()) {
            getItemHandler().setStackInSlot(slot, tinCan);
            return true;
        }
        if (existing.is(ItemRegistry.TIN_CAN.get()) && existing.getCount() < existing.getMaxStackSize()) {
            existing.grow(1);
            getItemHandler().setStackInSlot(slot, existing);
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        if (activeOperation != CannerOperationHelper.Operation.NONE) {
            tag.putString("ActiveOperation", activeOperation.name());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : getBaseProcessTime();
        activeOperation = tag.contains("ActiveOperation")
                ? CannerOperationHelper.Operation.valueOf(tag.getString("ActiveOperation"))
                : CannerOperationHelper.Operation.NONE;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return getMenuTitle();
    }
}
