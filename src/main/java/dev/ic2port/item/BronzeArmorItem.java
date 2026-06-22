package dev.ic2port.item;

import dev.ic2port.setup.ModArmorMaterials;
import net.minecraft.world.item.ArmorItem;

/** Bronze armor set — slightly below iron protection, repaired with bronze ingots. */
public class BronzeArmorItem extends ArmorItem {

    public BronzeArmorItem(final Type type, final Properties properties) {
        super(ModArmorMaterials.BRONZE, type, properties);
    }
}
