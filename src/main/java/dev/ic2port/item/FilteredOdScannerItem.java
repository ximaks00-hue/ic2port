package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.OreScannerHelper;
import dev.ic2port.util.ScannerItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filtered ore scanner — only reports ores matching the configured filter key.
 */
public class FilteredOdScannerItem extends ElectricItem {

    public static final double CAPACITY = 150_000.0D;
    public static final double SCAN_COST = 300.0D;
    public static final int USE_COOLDOWN_TICKS = 40;
    private static final String FILTER_TAG = "OreFilter";

    public FilteredOdScannerItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.MV);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            if (player.getCooldowns().isOnCooldown(this) || getStoredEnergy(stack) < SCAN_COST) {
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.pass(stack);
        }

        if (player.getCooldowns().isOnCooldown(this) || getStoredEnergy(stack) < SCAN_COST) {
            return InteractionResultHolder.fail(stack);
        }

        String filter = stack.getOrCreateTag().getString(FILTER_TAG);
        Map<String, Integer> counts = OreScannerHelper.scanColumn(level, player.blockPosition());
        ScannerItemHelper.finalizeScan(player, this, stack, this, SCAN_COST, USE_COOLDOWN_TICKS);

        if (!filter.isEmpty()) {
            Map<String, Integer> filtered = new LinkedHashMap<>();
            counts.forEach((key, value) -> {
                if (key.contains(filter) || filter.equals(key)) {
                    filtered.put(key, value);
                }
            });
            counts = filtered;
        }

        if (counts.isEmpty()) {
            ScannerItemHelper.showEmptyResult(player);
        } else {
            player.displayClientMessage(OreScannerHelper.formatResult(counts), true);
        }
        return InteractionResultHolder.success(stack);
    }

    public static void setFilter(final ItemStack stack, final String filterKey) {
        stack.getOrCreateTag().putString(FILTER_TAG, filterKey);
    }
}
