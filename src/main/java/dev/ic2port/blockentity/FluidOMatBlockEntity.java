package dev.ic2port.blockentity;

import dev.ic2port.item.FluidCellItem;
import dev.ic2port.menu.FluidOMatMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.FluidOMatHelper;
import dev.ic2port.util.FullInventoryAccess;
import dev.ic2port.util.TradeOMatHelper;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Automated fluid trade station linked to a nearby personal tank.
 */
public class FluidOMatBlockEntity extends BlockEntity implements MenuProvider, FullInventoryAccess {

    public static final int SLOT_CELL_IN = 0;
    public static final int SLOT_PAYMENT = 1;
    public static final int SLOT_CELL_OUT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int MB_PER_CELL = FluidCellItem.CAPACITY_MB;

    private final net.minecraftforge.items.ItemStackHandler itemHandler =
            new net.minecraftforge.items.ItemStackHandler(SLOT_COUNT) {
                @Override
                protected void onContentsChanged(final int slot) {
                    setChanged();
                }

                @Override
                public boolean isItemValid(final int slot, final ItemStack stack) {
                    return slot != SLOT_CELL_OUT;
                }
            };

    @Nullable
    private UUID ownerUuid;
    private int priceCoins = 1;
    @Nullable
    private BlockPos linkedTankPos;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> priceCoins;
                case 1 -> linkedTankPos == null ? 0 : 1;
                case 2 -> {
                    PersonalTankBlockEntity tank = getLinkedTank();
                    yield tank == null ? 0 : tank.getTank().getFluidAmount();
                }
                case 3 -> {
                    PersonalTankBlockEntity tank = getLinkedTank();
                    yield tank == null ? 0 : tank.getTank().getCapacity();
                }
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

    public FluidOMatBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.FLUID_O_MAT_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final FluidOMatBlockEntity mat) {
        mat.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        refreshLinkedTank();
        tryProcessTrade();
    }

    private void refreshLinkedTank() {
        if (level == null) {
            return;
        }
        PersonalTankBlockEntity tank = FluidOMatHelper.findLinkedTank(level, worldPosition);
        linkedTankPos = tank == null ? null : tank.getBlockPos();
    }

    private void tryProcessTrade() {
        if (level == null || priceCoins <= 0) {
            return;
        }
        ItemStack cellIn = itemHandler.getStackInSlot(SLOT_CELL_IN);
        ItemStack payment = itemHandler.getStackInSlot(SLOT_PAYMENT);
        ItemStack cellOut = itemHandler.getStackInSlot(SLOT_CELL_OUT);
        if (!isEmptyFluidCell(cellIn) || payment.isEmpty() || !cellOut.isEmpty()) {
            return;
        }
        if (TradeOMatHelper.countCoinValue(payment) < priceCoins) {
            return;
        }
        PersonalTankBlockEntity tank = getLinkedTank();
        if (tank == null) {
            return;
        }
        Fluid fluid = FluidOMatHelper.getStoredFluid(tank);
        if (fluid == null) {
            return;
        }
        if (!FluidOMatHelper.drainMillibuckets(tank, MB_PER_CELL)) {
            return;
        }
        payment.shrink(consumeCoins(payment, priceCoins));
        itemHandler.setStackInSlot(SLOT_PAYMENT, payment.isEmpty() ? ItemStack.EMPTY : payment);
        itemHandler.setStackInSlot(SLOT_CELL_IN, ItemStack.EMPTY);
        itemHandler.setStackInSlot(SLOT_CELL_OUT, FluidCellItem.createFilled(
                ItemRegistry.FLUID_CELL.get(), fluid, MB_PER_CELL));
        setChanged();
    }

    private static boolean isEmptyFluidCell(final ItemStack stack) {
        return stack.is(ItemRegistry.FLUID_CELL.get()) && FluidCellItem.isEmpty(stack);
    }

    private static int consumeCoins(final ItemStack payment, final int price) {
        int remaining = price;
        int consumedStacks = 0;
        while (remaining > 0 && consumedStacks < payment.getCount()) {
            int value = TradeOMatHelper.countCoinValue(payment.copyWithCount(1));
            if (value <= 0 || value > remaining) {
                break;
            }
            remaining -= value;
            consumedStacks++;
        }
        return consumedStacks;
    }

    @Nullable
    public PersonalTankBlockEntity getLinkedTank() {
        if (level == null || linkedTankPos == null) {
            return null;
        }
        if (level.getBlockEntity(linkedTankPos) instanceof PersonalTankBlockEntity tank) {
            return tank;
        }
        return null;
    }

    public void setPriceCoins(final int priceCoins) {
        this.priceCoins = Math.max(0, priceCoins);
        setChanged();
    }

    public void bindOwner(final Player player) {
        if (ownerUuid == null) {
            ownerUuid = player.getUUID();
            setChanged();
        }
    }

    public boolean isOwner(final Player player) {
        return ownerUuid == null || ownerUuid.equals(player.getUUID());
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public net.minecraftforge.items.ItemStackHandler getFullItemHandler() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.fluid_o_mat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        if (player instanceof ServerPlayer) {
            bindOwner(player);
            refreshLinkedTank();
        }
        return new FluidOMatMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("Price", priceCoins);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        if (linkedTankPos != null) {
            tag.putLong("LinkedTank", linkedTankPos.asLong());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        priceCoins = tag.contains("Price") ? tag.getInt("Price") : 1;
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        linkedTankPos = tag.contains("LinkedTank") ? BlockPos.of(tag.getLong("LinkedTank")) : null;
    }
}
