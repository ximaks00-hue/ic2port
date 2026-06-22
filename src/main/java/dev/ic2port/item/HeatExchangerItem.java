package dev.ic2port.item;

import dev.ic2port.api.reactor.IReactor;
import dev.ic2port.api.reactor.IReactorHeatStorage;
import dev.ic2port.util.ReactorComponentHeat;
import dev.ic2port.util.ReactorGridHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Balances heat between the reactor hull and adjacent heat-storing components.
 */
public class HeatExchangerItem extends Item implements IReactorHeatStorage {

    private final double transferPerTick;
    private final double maxComponentHeat;

    public HeatExchangerItem(
            final Properties properties,
            final double transferPerTick,
            final double maxComponentHeat) {
        super(properties);
        this.transferPerTick = transferPerTick;
        this.maxComponentHeat = maxComponentHeat;
    }

    @Override
    public void processTick(final IReactor reactor, final ItemStack stack, final int x, final int y) {
        if (!reactor.isColumnEnabled(x)) {
            return;
        }

        balanceWithReactor(reactor, stack);
        ReactorGridHelper.forEachNeighbor(reactor, x, y, (neighborX, neighborY) -> {
            ItemStack neighborStack = reactor.getStack(neighborX, neighborY);
            if (!ReactorGridHelper.storesHeat(neighborStack)) {
                return;
            }
            balanceComponents(stack, neighborStack);
        });

        if (ReactorComponentHeat.isOverloaded(stack, maxComponentHeat)) {
            reactor.setStack(x, y, ItemStack.EMPTY);
        }
    }

    private void balanceWithReactor(final IReactor reactor, final ItemStack stack) {
        double reactorHeat = reactor.getHeat();
        double selfHeat = ReactorComponentHeat.getHeat(stack);
        if (reactorHeat > selfHeat) {
            double move = Math.min(transferPerTick, reactorHeat - selfHeat);
            move = reactor.removeHeat(move);
            ReactorComponentHeat.addHeat(stack, move, maxComponentHeat);
        } else if (selfHeat > reactorHeat) {
            double move = Math.min(transferPerTick, selfHeat - reactorHeat);
            ReactorComponentHeat.addHeat(stack, -move, maxComponentHeat);
            reactor.addHeat(move);
        }
    }

    private void balanceComponents(final ItemStack self, final ItemStack neighbor) {
        if (!(neighbor.getItem() instanceof IReactorHeatStorage neighborStorage)) {
            return;
        }

        double selfHeat = ReactorComponentHeat.getHeat(self);
        double neighborHeat = ReactorComponentHeat.getHeat(neighbor);
        if (selfHeat == neighborHeat) {
            return;
        }

        double selfMax = getMaxComponentHeat(self);
        double neighborMax = neighborStorage.getMaxComponentHeat(neighbor);
        double move = Math.min(transferPerTick, Math.abs(selfHeat - neighborHeat) / 2.0D);
        if (selfHeat > neighborHeat) {
            move = Math.min(move, selfHeat);
            double neighborSpace = neighborMax - neighborHeat;
            move = Math.min(move, neighborSpace);
            ReactorComponentHeat.addHeat(self, -move, selfMax);
            ReactorComponentHeat.addHeat(neighbor, move, neighborMax);
        } else {
            move = Math.min(move, neighborHeat);
            double selfSpace = selfMax - selfHeat;
            move = Math.min(move, selfSpace);
            ReactorComponentHeat.addHeat(neighbor, -move, neighborMax);
            ReactorComponentHeat.addHeat(self, move, selfMax);
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
        tooltip.add(Component.translatable("item.ic2port.reactor_component.exchanger", (int) transferPerTick)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.reactor_component.heat", (int) ReactorComponentHeat.getHeat(stack), (int) maxComponentHeat)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
