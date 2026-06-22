package dev.ic2port.item;

import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes {@link ModCapabilities#ENERGY_NODE_CAPABILITY} on electric item stacks.
 */
public class ElectricItemCapabilityProvider implements ICapabilityProvider {

    private final ItemStack stack;
    private final IElectricItem item;
    private final LazyOptional<ItemEnergyStorage> energyOptional;

    public ElectricItemCapabilityProvider(final ItemStack stack, final IElectricItem item) {
        this.stack = stack;
        this.item = item;
        this.energyOptional = LazyOptional.of(() -> new ItemEnergyStorage(stack, item));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull net.minecraftforge.common.capabilities.Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return LazyOptional.empty();
    }

    private static final class ItemEnergyStorage implements IEnergyAcceptor {

        private final ItemStack stack;
        private final IElectricItem item;

        private ItemEnergyStorage(final ItemStack stack, final IElectricItem item) {
            this.stack = stack;
            this.item = item;
        }

        @Override
        public double getCapacity() {
            return item.getMaxEnergy();
        }

        @Override
        public double getStoredEnergy() {
            return item.getStoredEnergy(stack);
        }

        @Override
        public int getTier() {
            return item.getTier();
        }

        @Override
        public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
            if (tier < item.getTier()) {
                return amount;
            }
            return item.charge(stack, amount);
        }
    }
}
