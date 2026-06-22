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
 * MOX fuel rod — EU output scales with reactor hull heat; runs hotter when hot.
 */
public class MoxFuelRodItem extends RadioactiveItem implements IReactorFuel {

    public static final String DEPLETION_TAG = "Depletion";
    public static final int MAX_DEPLETION = 15_000;

    public MoxFuelRodItem(final Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x) || isDepleted(stack)) {
            return;
        }

        int neighborRods = ReactorMath.countAdjacentFuelRods(reactor, x, y);
        int reflectorBonus = ReactorMath.reflectorBonusForCell(reactor, x, y);
        int pulses = ReactorMath.pulsesForNeighbors(neighborRods) + reflectorBonus;
        double heatRatio = reactor.getHeat() / reactor.getMaxHeat();
        double euMult = ReactorMath.moxEnergyMultiplier(heatRatio);
        double huMult = ReactorMath.moxHeatMultiplier(heatRatio);

        reactor.addGeneratedEnergy(ReactorMath.energyForPulses(pulses) * euMult);
        reactor.addHeat(ReactorMath.heatForPulses(pulses) * huMult);

        int newDepletion = getDepletion(stack) + pulses;
        if (newDepletion >= MAX_DEPLETION) {
            reactor.setStack(x, y, new ItemStack(ItemRegistry.DEPLETED_FUEL_ROD.get()));
        } else {
            setDepletion(stack, newDepletion);
        }
    }

    @Override
    public int getMaxDepletion() {
        return MAX_DEPLETION;
    }

    @Override
    public int getDepletion(final ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(DEPLETION_TAG) : 0;
    }

    public static void setDepletion(final ItemStack stack, final int depletion) {
        stack.getOrCreateTag().putInt(DEPLETION_TAG, Math.max(0, Math.min(MAX_DEPLETION, depletion)));
    }

    @Override
    public boolean isDepleted(final ItemStack stack) {
        return getDepletion(stack) >= MAX_DEPLETION;
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
        return net.minecraft.util.Mth.hsvToRgb(0.02F, 1.0F, 0.4F + ratio * 0.6F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int remaining = MAX_DEPLETION - getDepletion(stack);
        tooltip.add(Component.translatable("item.ic2port.mox_fuel_rod.condition", remaining, MAX_DEPLETION)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.mox_fuel_rod.heat_bonus")
                .withStyle(ChatFormatting.GOLD));
        if (isDepleted(stack)) {
            tooltip.add(Component.translatable("item.ic2port.fuel_rod.depleted")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }
}
