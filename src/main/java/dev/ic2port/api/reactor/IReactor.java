package dev.ic2port.api.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Reactor grid access for {@link dev.ic2port.item.IReactorComponent} simulation.
 */
public interface IReactor {

    int getGridWidth();

    int getGridHeight();

    boolean isInBounds(final int x, final int y);

    ItemStack getStack(final int x, final int y);

    void setStack(final int x, final int y, final ItemStack stack);

    double getHeat();

    void addHeat(final double amount);

    double getMaxHeat();

    void addGeneratedEnergy(final double amount);

    /**
     * @return heat actually removed from the reactor pool
     */
    double removeHeat(final double amount);

    int getChamberCount();

    boolean isColumnEnabled(final int x);

    Level getLevel();

    BlockPos getPosition();
}
