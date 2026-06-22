package dev.ic2port.item;

import dev.ic2port.setup.ModArmorMaterials;
import net.minecraft.world.item.ArmorItem;

/** Composite armor set — high-tier alloy plating, repaired with advanced alloy. */
public class CompositeArmorItem extends ArmorItem {

    public CompositeArmorItem(final Type type, final Properties properties) {
        super(ModArmorMaterials.COMPOSITE, type, properties);
    }
}
