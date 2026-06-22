package dev.ic2port.item;

import dev.ic2port.menu.ToolboxMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

/**
 * Portable filtered storage for cables, tools and upgrade modules.
 */
public class ToolboxItem extends Item {

    public static final int SLOT_COUNT = 18;
    private static final String INVENTORY_TAG = "ToolboxInventory";

    public ToolboxItem(final Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int inventorySlot = player.getInventory().selected;
            ItemStack held = player.getInventory().getItem(inventorySlot);
            if (!(held.getItem() instanceof ToolboxItem) || !ItemStack.isSameItemSameTags(held, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            NetworkHooks.openScreen(
                    serverPlayer,
                    new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return stack.getHoverName();
                        }

                        @Override
                        public AbstractContainerMenu createMenu(
                                final int containerId,
                                final Inventory inventory,
                                final Player menuPlayer) {
                            return new ToolboxMenu(containerId, inventory, inventorySlot);
                        }
                    },
                    buffer -> buffer.writeVarInt(inventorySlot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static ItemStackHandler createItemHandler(final Inventory inventory, final int toolboxSlot) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack candidate) {
                return dev.ic2port.util.ToolboxFilters.isAllowed(candidate);
            }

            @Override
            protected void onContentsChanged(final int slot) {
                saveInventory(inventory.getItem(toolboxSlot), this);
            }
        };
        loadInventory(inventory.getItem(toolboxSlot), handler);
        return handler;
    }

    public static void loadInventory(final ItemStack toolboxStack, final ItemStackHandler handler) {
        CompoundTag tag = toolboxStack.getTag();
        if (tag != null && tag.contains(INVENTORY_TAG, Tag.TAG_COMPOUND)) {
            handler.deserializeNBT(tag.getCompound(INVENTORY_TAG));
        }
    }

    public static void saveInventory(final ItemStack toolboxStack, final ItemStackHandler handler) {
        toolboxStack.getOrCreateTag().put(INVENTORY_TAG, handler.serializeNBT());
    }
}
