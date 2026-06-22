package dev.ic2port.item;

import dev.ic2port.menu.AutoFeederMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stores filled tin cans and auto-feeds the player when hungry (IC2 auto feeder module).
 */
public class AutoFeederModuleItem extends StorageArmorModuleItem {

    public static final int SLOT_COUNT = 4;
    public static final int HUNGER_THRESHOLD = 17;
    public static final int FEED_INTERVAL_TICKS = 20;

    private static final String INVENTORY_TAG = "AutoFeederInventory";

    public AutoFeederModuleItem(final Properties properties) {
        super(properties);
    }

    @Override
    protected String getInventoryTag() {
        return INVENTORY_TAG;
    }

    @Override
    protected int getSlotCount() {
        return SLOT_COUNT;
    }

    @Override
    protected Component getGuiTitle() {
        return Component.translatable("item.ic2port.auto_feeder_module");
    }

    @Override
    protected AbstractContainerMenu openModuleMenu(
            final int containerId,
            final int moduleSlot,
            final Inventory inventory) {
        return new AutoFeederMenu(containerId, inventory, moduleSlot);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.auto_feeder_module.hint"));
    }

    public static boolean feedPlayerIfHungry(final Player player, final ItemStack moduleStack) {
        if (!(moduleStack.getItem() instanceof AutoFeederModuleItem module)) {
            return false;
        }
        return module.tryFeedPlayer(player, moduleStack);
    }
}
