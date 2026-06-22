package dev.ic2port.item;

import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.ArmorModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base item for armor modules that store filled tin cans.
 */
public abstract class StorageArmorModuleItem extends ArmorModuleItem {

    protected StorageArmorModuleItem(final Properties properties) {
        super(properties);
    }

    protected abstract String getInventoryTag();

    protected abstract int getSlotCount();

    protected abstract Component getGuiTitle();

    protected abstract AbstractContainerMenu openModuleMenu(
            final int containerId,
            final int moduleSlot,
            final Inventory inventory);

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int inventorySlot = player.getInventory().selected;
            ItemStack held = player.getInventory().getItem(inventorySlot);
            if (!ItemStack.isSameItemSameTags(held, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            final int moduleSlot = inventorySlot;
            NetworkHooks.openScreen(
                    serverPlayer,
                    new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return getGuiTitle();
                        }

                        @Override
                        public AbstractContainerMenu createMenu(
                                final int containerId,
                                final Inventory inventory,
                                final Player menuPlayer) {
                            return openModuleMenu(containerId, moduleSlot, inventory);
                        }
                    },
                    buffer -> buffer.writeVarInt(moduleSlot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public ItemStackHandler createItemHandler(final ItemStack moduleStack) {
        ItemStackHandler handler = new ItemStackHandler(getSlotCount()) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack candidate) {
                return candidate.is(ItemRegistry.FILLED_TIN_CAN.get());
            }

            @Override
            protected void onContentsChanged(final int slot) {
                saveInventory(moduleStack, this);
            }
        };
        loadInventory(moduleStack, handler);
        return handler;
    }

    public ItemStackHandler createItemHandler(final Inventory inventory, final int moduleSlot) {
        ItemStack moduleStack = inventory.getItem(moduleSlot);
        ItemStackHandler handler = new ItemStackHandler(getSlotCount()) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack candidate) {
                return candidate.is(ItemRegistry.FILLED_TIN_CAN.get());
            }

            @Override
            protected void onContentsChanged(final int slot) {
                saveInventory(inventory.getItem(moduleSlot), this);
            }
        };
        loadInventory(moduleStack, handler);
        return handler;
    }

    public void loadInventory(final ItemStack moduleStack, final ItemStackHandler handler) {
        CompoundTag tag = moduleStack.getTag();
        if (tag != null && tag.contains(getInventoryTag(), Tag.TAG_COMPOUND)) {
            handler.deserializeNBT(tag.getCompound(getInventoryTag()));
        }
    }

    public void saveInventory(final ItemStack moduleStack, final ItemStackHandler handler) {
        moduleStack.getOrCreateTag().put(getInventoryTag(), handler.serializeNBT());
    }

    public boolean tryFeedPlayer(final Player player, final ItemStack moduleStack) {
        ItemStackHandler handler = new ItemStackHandler(getSlotCount());
        loadInventory(moduleStack, handler);
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack can = handler.getStackInSlot(slot);
            if (!can.is(ItemRegistry.FILLED_TIN_CAN.get())) {
                continue;
            }
            FilledTinCanItem.feedPlayer(player, can);
            handler.setStackInSlot(slot, can);
            saveInventory(moduleStack, handler);
            return true;
        }
        return false;
    }

    public boolean tryFeedFromStorage(final ItemStack feederStack, final ItemStack storageStack) {
        ItemStackHandler feeder = new ItemStackHandler(getSlotCount());
        loadInventory(feederStack, feeder);
        if (hasFilledCan(feeder)) {
            return false;
        }
        ItemStackHandler storage = new ItemStackHandler(getSlotCount());
        if (storageStack.getItem() instanceof StorageArmorModuleItem storageItem) {
            storageItem.loadInventory(storageStack, storage);
        } else {
            return false;
        }
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack can = storage.getStackInSlot(slot);
            if (!can.is(ItemRegistry.FILLED_TIN_CAN.get())) {
                continue;
            }
            ItemStack moved = can.copyWithCount(1);
            can.shrink(1);
            storage.setStackInSlot(slot, can);
            insertCan(feederStack, feeder, moved);
            storageItem.saveInventory(storageStack, storage);
            return true;
        }
        return false;
    }

    private void insertCan(final ItemStack moduleStack, final ItemStackHandler handler, final ItemStack can) {
        for (int slot = 0; slot < getSlotCount(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (existing.isEmpty()) {
                handler.setStackInSlot(slot, can);
                saveInventory(moduleStack, handler);
                return;
            }
            if (ItemStack.isSameItemSameTags(existing, can) && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(1);
                saveInventory(moduleStack, handler);
                return;
            }
        }
    }

    private static boolean hasFilledCan(final ItemStackHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot).is(ItemRegistry.FILLED_TIN_CAN.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.armor_module.install_hint"));
    }
}
