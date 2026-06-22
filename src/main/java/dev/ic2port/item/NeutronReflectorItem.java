package dev.ic2port.item;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorComponent;
import dev.ic2port.util.ReactorComponentHeat;
import net.minecraft.util.Mth;
import dev.ic2port.util.ReactorMath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reflects neutrons from adjacent fuel rods, boosting their pulse count by +1 each.
 * Takes a reflection pulse per adjacent active rod and has limited durability.
 */
public class NeutronReflectorItem extends Item implements IReactorComponent {

    public static final double MAX_HEAT = 100_000.0D;
    private final boolean thick;

    public NeutronReflectorItem(final Properties properties, final boolean thick) {
        super(properties);
        this.thick = thick;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x)) {
            return;
        }
        int adjacent = ReactorMath.countAdjacentFuelRods(reactor, x, y);
        if (adjacent <= 0) {
            return;
        }
        double selfHeat = ReactorComponentHeat.getHeat(stack);
        double heatGain = adjacent * (thick ? 2.0D : 1.0D);
        ReactorComponentHeat.addHeat(stack, heatGain, MAX_HEAT);
        selfHeat = ReactorComponentHeat.getHeat(stack);
        if (selfHeat >= MAX_HEAT) {
            reactor.setStack(x, y, ItemStack.EMPTY);
        }
    }

    /**
     * @return extra pulse contribution to adjacent fuel rods (called by FuelRodItem scan)
     */
    public int getReflectionBonus() {
        return thick ? 2 : 1;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return ReactorComponentHeat.getHeat(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        double heat = ReactorComponentHeat.getHeat(stack);
        return Math.round(13.0F * (float) ((MAX_HEAT - heat) / MAX_HEAT));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (float) (ReactorComponentHeat.getHeat(stack) / MAX_HEAT);
        return Mth.hsvToRgb(0.58F - ratio * 0.45F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.neutron_reflector.bonus",
                        getReflectionBonus())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(
                        "item.ic2port.reactor_component.heat",
                        (int) ReactorComponentHeat.getHeat(stack),
                        (int) MAX_HEAT)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
