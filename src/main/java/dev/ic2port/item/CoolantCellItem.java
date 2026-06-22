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
 * Disposable heat sink that absorbs hull heat until it melts.
 */
public class CoolantCellItem extends Item implements IReactorHeatStorage {

    private final double capacity;
    private final double absorbPerTick;

    public CoolantCellItem(final Properties properties, final double capacity, final double absorbPerTick) {
        super(properties);
        this.capacity = capacity;
        this.absorbPerTick = absorbPerTick;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x)) {
            return;
        }

        double selfHeat = ReactorComponentHeat.getHeat(stack);
        if (selfHeat >= capacity) {
            reactor.setStack(x, y, ItemStack.EMPTY);
            return;
        }

        double space = capacity - selfHeat;
        double absorbed = reactor.removeHeat(Math.min(absorbPerTick, space));
        ReactorComponentHeat.addHeat(stack, absorbed, capacity);

        if (ReactorComponentHeat.getHeat(stack) >= capacity) {
            reactor.setStack(x, y, ItemStack.EMPTY);
        }
    }

    @Override
    public double getMaxComponentHeat(final ItemStack stack) {
        return capacity;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return ReactorComponentHeat.getHeat(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        double heat = ReactorComponentHeat.getHeat(stack);
        return Math.round(13.0F * (float) ((capacity - heat) / capacity));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (float) (ReactorComponentHeat.getHeat(stack) / capacity);
        return net.minecraft.util.Mth.hsvToRgb(0.58F - ratio * 0.45F, 0.8F, 1.0F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.reactor_component.coolant", (int) capacity, (int) absorbPerTick)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.reactor_component.heat", (int) ReactorComponentHeat.getHeat(stack), (int) capacity)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
