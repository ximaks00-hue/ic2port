package dev.ic2port.item;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorFuel;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.ReactorMath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Four uranium cells in one slot — counts as 4 fuel rods for neighbour pulse purposes.
 */
public class QuadFuelRodItem extends RadioactiveItem implements IReactorFuel {

    public static final int RODS = 4;
    public static final int MAX_DEPLETION = FuelRodItem.MAX_DEPLETION;

    public QuadFuelRodItem(final Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public int getMaxDepletion() {
        return MAX_DEPLETION;
    }

    @Override
    public int getDepletion(final ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(FuelRodItem.DEPLETION_TAG) : 0;
    }

    @Override
    public boolean isDepleted(final ItemStack stack) {
        return getDepletion(stack) >= MAX_DEPLETION;
    }

    @Override
    public int getRodCount() {
        return RODS;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x) || isDepleted(stack)) {
            return;
        }
        for (int rod = 0; rod < RODS; rod++) {
            int neighborRods = ReactorMath.countAdjacentFuelRods(reactor, x, y);
            int reflectorBonus = ReactorMath.reflectorBonusForCell(reactor, x, y);
            int pulses = ReactorMath.pulsesForNeighbors(neighborRods) + reflectorBonus;
            reactor.addGeneratedEnergy(ReactorMath.energyForPulses(pulses));
            reactor.addHeat(ReactorMath.heatForPulses(pulses));
        }
        int newDepletion = getDepletion(stack) + RODS;
        if (newDepletion >= MAX_DEPLETION) {
            reactor.setStack(x, y, new ItemStack(ItemRegistry.DEPLETED_FUEL_ROD.get()));
        } else {
            stack.getOrCreateTag().putInt(FuelRodItem.DEPLETION_TAG, newDepletion);
        }
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        int remaining = MAX_DEPLETION - getDepletion(stack);
        return Math.round(13.0F * remaining / MAX_DEPLETION);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (MAX_DEPLETION - getDepletion(stack)) / (float) MAX_DEPLETION;
        return net.minecraft.util.Mth.hsvToRgb(0.08F, 1.0F, 0.4F + ratio * 0.6F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int remaining = MAX_DEPLETION - getDepletion(stack);
        tooltip.add(Component.translatable("item.ic2port.fuel_rod.condition", remaining, MAX_DEPLETION)
                .withStyle(ChatFormatting.GRAY));
    }
}
