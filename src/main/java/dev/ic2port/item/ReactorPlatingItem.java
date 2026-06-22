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
 * Adds {@value #CAPACITY_BONUS} EU/t worth of hull heat capacity.
 * Absorbs heat passively — self-destructs if overheated.
 */
public class ReactorPlatingItem extends Item implements IReactorHeatStorage {

    public static final double CAPACITY_BONUS = 1_000.0D;
    public static final double MAX_COMPONENT_HEAT = 1_500.0D;
    private static final double ABSORB_PER_TICK = 1.0D;

    public ReactorPlatingItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x)) {
            return;
        }
        double heat = ReactorComponentHeat.getHeat(stack);
        if (heat >= MAX_COMPONENT_HEAT) {
            reactor.setStack(x, y, ItemStack.EMPTY);
            return;
        }
        double absorbed = reactor.removeHeat(Math.min(ABSORB_PER_TICK, MAX_COMPONENT_HEAT - heat));
        ReactorComponentHeat.addHeat(stack, absorbed, MAX_COMPONENT_HEAT);
    }

    @Override
    public double getMaxComponentHeat(final ItemStack stack) {
        return MAX_COMPONENT_HEAT;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return ReactorComponentHeat.getHeat(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        double heat = ReactorComponentHeat.getHeat(stack);
        return Math.round(13.0F * (float) ((MAX_COMPONENT_HEAT - heat) / MAX_COMPONENT_HEAT));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (float) (ReactorComponentHeat.getHeat(stack) / MAX_COMPONENT_HEAT);
        return net.minecraft.util.Mth.hsvToRgb(0.58F - ratio * 0.45F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.reactor_plating.capacity", (int) CAPACITY_BONUS)
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(
                        "item.ic2port.reactor_component.heat",
                        (int) ReactorComponentHeat.getHeat(stack),
                        (int) MAX_COMPONENT_HEAT)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
