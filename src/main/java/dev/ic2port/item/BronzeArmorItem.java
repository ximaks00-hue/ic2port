package dev.ic2port.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;

/** Bronze armor set — iron-tier protection, bronze repair material. */
public class BronzeArmorItem extends ArmorItem {

    public BronzeArmorItem(final Type type, final Properties properties) {
        super(ArmorMaterials.IRON, type, properties);
    }
}
