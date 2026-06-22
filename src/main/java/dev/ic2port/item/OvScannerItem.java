package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.OreScannerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * OV ore scanner — wider scan area and reports largest vein size (HV upgrade of OD scanner).
 */
public class OvScannerItem extends ElectricItem {

    public static final double CAPACITY = 200_000.0D;
    public static final double SCAN_COST = 1000.0D;
    public static final int USE_COOLDOWN_TICKS = 60;

    public OvScannerItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.HV);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return player.getCooldowns().isOnCooldown(this) || getStoredEnergy(stack) < SCAN_COST
                    ? InteractionResultHolder.fail(stack) : InteractionResultHolder.pass(stack);
        }
        if (player.getCooldowns().isOnCooldown(this) || getStoredEnergy(stack) < SCAN_COST) {
            player.displayClientMessage(Component.translatable("message.ic2port.ov_scanner.no_energy"), true);
            return InteractionResultHolder.fail(stack);
        }

        OreScannerHelper.ScanResult result = OreScannerHelper.scanDetailed(level, player.blockPosition());
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        drawEnergy(stack, SCAN_COST);

        if (result.counts().isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ic2port.od_scanner.empty"), true);
        } else {
            player.displayClientMessage(OreScannerHelper.formatResult(result.counts()), true);
            if (result.dominantOreKey() != null) {
                player.displayClientMessage(Component.translatable("message.ic2port.ov_scanner.vein",
                        Component.translatable(result.dominantOreKey()), result.maxVeinSize()), true);
            }
        }
        return InteractionResultHolder.success(stack);
    }
}
