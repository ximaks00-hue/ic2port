package dev.ic2port.item;

import dev.ic2port.util.ArmorSetHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;

/**
 * Hazmat armor piece that contributes to radiation shielding when worn as a full set.
 */
public class HazmatArmorItem extends ArmorItem {

    public HazmatArmorItem(final Type type, final Properties properties) {
        super(ArmorMaterials.LEATHER, type, properties);
    }

    public static boolean hasFullSet(final Player player) {
        return ArmorSetHelper.hasFullTypedSet(player, HazmatArmorItem.class);
    }
}
