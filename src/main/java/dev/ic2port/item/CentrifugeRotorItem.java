package dev.ic2port.item;

import dev.ic2port.setup.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Consumable rotor for the thermal centrifuge — wears down during processing and breaks on overheat.
 */
public class CentrifugeRotorItem extends Item {

    public static final int MAX_DURABILITY = 2800;

    public CentrifugeRotorItem(final Properties properties) {
        super(properties.stacksTo(1).durability(MAX_DURABILITY));
    }

    public static boolean isRotorStack(final ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemRegistry.CENTRIFUGE_ROTOR.get());
    }

    public static boolean isUsable(final ItemStack stack) {
        return isRotorStack(stack) && stack.getDamageValue() < stack.getMaxDamage();
    }

    public static void applyWear(final ItemStack stack, final int amount) {
        if (!isRotorStack(stack) || amount <= 0) {
            return;
        }
        int nextDamage = stack.getDamageValue() + amount;
        if (nextDamage >= stack.getMaxDamage()) {
            stack.setCount(0);
        } else {
            stack.setDamageValue(nextDamage);
        }
    }
}
