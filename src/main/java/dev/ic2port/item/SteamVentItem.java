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
 * Steam-reactor heat vent — pulls hull heat and vents steam pressure passively.
 */
public class SteamVentItem extends Item implements IReactorHeatStorage {

    private final double pullPerTick;
    private final double maxComponentHeat;
    private final double passiveCoolPerSecond;

    public SteamVentItem(
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
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.steam_vent.desc", (int) pullPerTick, (int) passiveCoolPerSecond)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.reactor_component.heat",
                        (int) ReactorComponentHeat.getHeat(stack), (int) maxComponentHeat)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
