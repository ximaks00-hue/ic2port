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

import java.util.Map;

/**
 * Ranged ore density scanner — wider scan area than the standard OD scanner.
 */
public class OdScannerProItem extends ElectricItem {

    public static final double CAPACITY = 250_000.0D;
    public static final double SCAN_COST = 500.0D;
    public static final int USE_COOLDOWN_TICKS = 60;
    public static final int HORIZONTAL_RADIUS = 12;
    public static final int DEPTH = 64;

    public OdScannerProItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.HV);
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

        OreScannerHelper.ScanResult result = OreScannerHelper.scanDetailed(level, player.blockPosition());
        Map<String, Integer> counts = result.counts();
        ScannerItemHelper.finalizeScan(player, this, stack, this, SCAN_COST, USE_COOLDOWN_TICKS);

        if (counts.isEmpty()) {
            ScannerItemHelper.showEmptyResult(player);
        } else {
            player.displayClientMessage(OreScannerHelper.formatResult(counts), true);
        }
        return InteractionResultHolder.success(stack);
    }
}
