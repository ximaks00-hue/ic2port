package dev.ic2port.blockentity;

import dev.ic2port.menu.TradeOMatMenu;
import dev.ic2port.network.packet.TradeOMatBuyerViewS2CPacket;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.ContainerDataHelper;
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
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Automated trade station linked to a nearby personal chest.
 */
public class TradeOMatBlockEntity extends BlockEntity implements MenuProvider, FullInventoryAccess {

    public static final int SLOT_OFFER = 0;
    public static final int SLOT_PAYMENT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }
    };

    @Nullable
    private UUID ownerUuid;
    private int priceCoins;
    @Nullable
    private BlockPos linkedChestPos;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> priceCoins;
                case 1 -> linkedChestPos == null ? 0 : 1;
                case 2 -> buyerViewMask;
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    private int buyerViewMask;

    public TradeOMatBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.TRADE_O_MAT_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final TradeOMatBlockEntity mat) {
        mat.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        refreshLinkedChest();
        tryProcessTrade();
    }

    private void refreshLinkedChest() {
        if (level == null) {
            return;
        }
        PersonalChestBlockEntity chest = TradeOMatHelper.findLinkedChest(level, worldPosition);
        linkedChestPos = chest == null ? null : chest.getBlockPos();
    }

    private void tryProcessTrade() {
        if (level == null || priceCoins <= 0) {
            return;
        }
        ItemStack offer = itemHandler.getStackInSlot(SLOT_OFFER);
        ItemStack payment = itemHandler.getStackInSlot(SLOT_PAYMENT);
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (offer.isEmpty() || payment.isEmpty() || !output.isEmpty()) {
            return;
        }
        if (TradeOMatHelper.countCoinValue(payment) < priceCoins) {
            return;
        }
        PersonalChestBlockEntity chest = getLinkedChest();
        if (chest == null) {
            return;
        }
        if (!TradeOMatHelper.extractFromChest(chest.getFullItemHandler(), offer.copyWithCount(1))) {
            return;
        }
        payment.shrink(consumeCoins(payment, priceCoins));
        itemHandler.setStackInSlot(SLOT_PAYMENT, payment.isEmpty() ? ItemStack.EMPTY : payment);
        itemHandler.setStackInSlot(SLOT_OUTPUT, offer.copyWithCount(1));
        setChanged();
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
    public PersonalChestBlockEntity getLinkedChest() {
        if (level == null || linkedChestPos == null) {
            return null;
        }
        if (level.getBlockEntity(linkedChestPos) instanceof PersonalChestBlockEntity chest) {
            return chest;
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

    public void toggleBuyerView(final ServerPlayer player) {
        int bit = 1 << (player.getId() & 31);
        buyerViewMask ^= bit;
        TradeOMatBuyerViewS2CPacket.send(player, worldPosition, (buyerViewMask & bit) != 0);
        setChanged();
    }

    public boolean isBuyerView(final Player player) {
        int bit = 1 << (player.getId() & 31);
        return !isOwner(player) || (buyerViewMask & bit) != 0;
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
        return Component.translatable("block.ic2port.trade_o_mat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            bindOwner(player);
            refreshLinkedChest();
        }
        return new TradeOMatMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("Price", priceCoins);
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        if (linkedChestPos != null) {
            tag.putLong("LinkedChest", linkedChestPos.asLong());
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        priceCoins = tag.getInt("Price");
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        linkedChestPos = tag.contains("LinkedChest") ? BlockPos.of(tag.getLong("LinkedChest")) : null;
    }
}
