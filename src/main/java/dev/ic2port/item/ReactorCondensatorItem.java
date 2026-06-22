package dev.ic2port.item;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.util.ReactorComponentHeat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Absorbs large amounts of reactor heat into itself before the hull is affected.
 * Two tiers: RSH (10k) and LZH (100k capacity).
 */
public class ReactorCondensatorItem extends Item implements IReactorHeatStorage {

    private final double maxComponentHeat;
    private final double absorptionRate;

    public ReactorCondensatorItem(final Properties properties, final double maxHeat, final double absorptionRate) {
        super(properties);
        this.maxComponentHeat = maxHeat;
        this.absorptionRate = absorptionRate;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        double selfHeat = ReactorComponentHeat.getHeat(stack);
        if (selfHeat >= maxComponentHeat) {
            reactor.setStack(x, y, ItemStack.EMPTY);
            return;
        }
        double space = maxComponentHeat - selfHeat;
        double toAbsorb = Math.min(absorptionRate, space);
        double absorbed = reactor.removeHeat(toAbsorb);
        if (absorbed > 0.0D) {
            ReactorComponentHeat.addHeat(stack, absorbed, maxComponentHeat);
        }
    }

    @Override
    public double getMaxComponentHeat(final ItemStack stack) {
        return maxComponentHeat;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return ReactorComponentHeat.getHeat(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        double heat = ReactorComponentHeat.getHeat(stack);
        return Math.round(13.0F * (float) ((maxComponentHeat - heat) / maxComponentHeat));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (float) (ReactorComponentHeat.getHeat(stack) / maxComponentHeat);
        return net.minecraft.util.Mth.hsvToRgb(0.58F - ratio * 0.45F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.reactor_condensator.capacity",
                        (int) maxComponentHeat, (int) absorptionRate)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(
                        "item.ic2port.reactor_component.heat",
                        (int) ReactorComponentHeat.getHeat(stack),
                        (int) maxComponentHeat)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
