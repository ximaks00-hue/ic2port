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
 * Pulls heat from the reactor hull into itself and vents it passively once per second.
 */
public class HeatVentItem extends Item implements IReactorHeatStorage {

    private final double pullPerTick;
    private final double maxComponentHeat;
    private final double passiveCoolPerSecond;

    public HeatVentItem(
            final Properties properties,
            final double pullPerTick,
            final double maxComponentHeat,
            final double passiveCoolPerSecond) {
        super(properties);
        this.pullPerTick = pullPerTick;
        this.maxComponentHeat = maxComponentHeat;
        this.passiveCoolPerSecond = passiveCoolPerSecond;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x)) {
            return;
        }

        double selfHeat = ReactorComponentHeat.getHeat(stack);
        if (selfHeat < maxComponentHeat) {
            double space = maxComponentHeat - selfHeat;
            double pulled = reactor.removeHeat(Math.min(pullPerTick, space));
            ReactorComponentHeat.addHeat(stack, pulled, maxComponentHeat);
            selfHeat = ReactorComponentHeat.getHeat(stack);
        }

        if (reactor.getLevel().getGameTime() % 20L == 0L && selfHeat > 0.0D) {
            double cooled = Math.min(passiveCoolPerSecond, selfHeat);
            ReactorComponentHeat.addHeat(stack, -cooled, maxComponentHeat);
            selfHeat = ReactorComponentHeat.getHeat(stack);
        }

        if (selfHeat >= maxComponentHeat) {
            reactor.setStack(x, y, ItemStack.EMPTY);
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
        tooltip.add(Component.translatable("item.ic2port.reactor_component.vent", (int) pullPerTick, (int) passiveCoolPerSecond)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.reactor_component.heat", (int) ReactorComponentHeat.getHeat(stack), (int) maxComponentHeat)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
