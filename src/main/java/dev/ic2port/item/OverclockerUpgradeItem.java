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
 * Speeds up IC2 machines at the cost of higher EU consumption per tick.
 */
public class OverclockerUpgradeItem extends Item implements IUpgradeItem {

    public OverclockerUpgradeItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.overclocker_upgrade.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
