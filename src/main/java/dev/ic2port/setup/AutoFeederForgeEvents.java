package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.AutoFeederModuleItem;
import dev.ic2port.item.FoodStorageModuleItem;
import dev.ic2port.item.StorageArmorModuleItem;
import dev.ic2port.util.ArmorModuleHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class AutoFeederForgeEvents {

    private AutoFeederForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {
            return;
        }

        Player player = event.player;
        if (player.tickCount % AutoFeederModuleItem.FEED_INTERVAL_TICKS != 0) {
            return;
        }
        if (player.getFoodData().getFoodLevel() >= AutoFeederModuleItem.HUNGER_THRESHOLD) {
            return;
        }

        ItemStack chestplate = ArmorModuleHelper.getChestplate(player);
        if (ArmorModuleHelper.acceptsModules(chestplate)) {
            List<ItemStack> modules = ArmorModuleHelper.getModules(chestplate);
            ItemStack feederStack = null;
            ItemStack storageStack = null;
            int feederIndex = -1;
            int storageIndex = -1;
            for (int index = 0; index < modules.size(); index++) {
                ItemStack module = modules.get(index);
                if (module.getItem() instanceof AutoFeederModuleItem) {
                    feederStack = module;
                    feederIndex = index;
                } else if (module.getItem() instanceof FoodStorageModuleItem) {
                    storageStack = module;
                    storageIndex = index;
                }
            }
            if (feederStack != null && storageStack != null
                    && feederStack.getItem() instanceof StorageArmorModuleItem feederModule) {
                if (feederModule.tryFeedFromStorage(feederStack, storageStack)) {
                    modules.set(feederIndex, feederStack);
                    modules.set(storageIndex, storageStack);
                    ArmorModuleHelper.setModules(chestplate, modules);
                    player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                }
            }
            if (feederStack != null && AutoFeederModuleItem.feedPlayerIfHungry(player, feederStack)) {
                modules.set(feederIndex, feederStack);
                ArmorModuleHelper.setModules(chestplate, modules);
                player.setItemSlot(EquipmentSlot.CHEST, chestplate);
                return;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof AutoFeederModuleItem
                    && AutoFeederModuleItem.feedPlayerIfHungry(player, stack)) {
                return;
            }
        }
    }
}
