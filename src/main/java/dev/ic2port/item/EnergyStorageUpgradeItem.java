package dev.ic2port.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Expands the internal EU buffer of IC2 machines.
 */
public class EnergyStorageUpgradeItem extends Item implements IUpgradeItem {

    public EnergyStorageUpgradeItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.energy_storage_upgrade.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
