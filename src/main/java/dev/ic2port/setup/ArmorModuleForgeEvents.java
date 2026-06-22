package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import dev.ic2port.menu.ArmorModulesMenu;
import dev.ic2port.util.ArmorModuleHelper;
import dev.ic2port.util.JetpackHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ArmorModuleForgeEvents {

    private ArmorModuleForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onRightClickItem(final PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ArmorItem armor) || armor.getType() != ArmorItem.Type.CHESTPLATE) {
            return;
        }
        if (!(stack.getItem() instanceof NanoSuitItem) && !(stack.getItem() instanceof QuantumSuitItem)) {
            return;
        }
        openModuleScreen(event.getEntity(), event.getEntity().getInventory().selected, stack.getHoverName());
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRightClickEmpty(final PlayerInteractEvent.RightClickEmpty event) {
        if (!event.getEntity().isShiftKeyDown()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack worn = ArmorModuleHelper.getChestplate(player);
        if (!ArmorModuleHelper.acceptsModules(worn)) {
            return;
        }

        if (player.onGround()) {
            openModuleScreen(player, ArmorModuleHelper.WORN_CHEST_INVENTORY_SLOT, worn.getHoverName());
        } else {
            JetpackHelper.cycleInstalledJetpackMode(player);
        }
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }

    private static void openModuleScreen(final Player player, final int chestSlot, final Component title) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        NetworkHooks.openScreen(
                serverPlayer,
                new net.minecraft.world.SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new ArmorModulesMenu(containerId, inventory, chestSlot),
                        title),
                buffer -> buffer.writeVarInt(chestSlot));
    }
}
