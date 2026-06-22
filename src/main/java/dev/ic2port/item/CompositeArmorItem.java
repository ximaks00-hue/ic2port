package dev.ic2port.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;

/** Composite armor set — netherite-tier alloy plating. */
public class CompositeArmorItem extends ArmorItem {

    public CompositeArmorItem(final Type type, final Properties properties) {
        super(ArmorMaterials.NETHERITE, type, properties);
    }
}
