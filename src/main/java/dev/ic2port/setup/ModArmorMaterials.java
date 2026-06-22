package dev.ic2port.setup;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;

/**
 * Custom armor materials for IC2 armor sets.
 */
public final class ModArmorMaterials {

    private static final EnumMap<ArmorItem.Type, Integer> BRONZE_DEFENSE = defense(
            2, 4, 5, 2);
    private static final EnumMap<ArmorItem.Type, Integer> COMPOSITE_DEFENSE = defense(
            3, 6, 8, 3);

    public static final ArmorMaterial BRONZE = material(
            "bronze",
            BRONZE_DEFENSE,
            18,
            8,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            "bronze_ingot");

    public static final ArmorMaterial COMPOSITE = material(
            "composite",
            COMPOSITE_DEFENSE,
            40,
            12,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            2.0F,
            0.1F,
            "advanced_alloy");

    private ModArmorMaterials() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static EnumMap<ArmorItem.Type, Integer> defense(
            final int boots,
            final int leggings,
            final int chestplate,
            final int helmet) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, boots);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.HELMET, helmet);
        });
    }

    private static ArmorMaterial material(
            final String name,
            final EnumMap<ArmorItem.Type, Integer> defense,
            final int durabilityMultiplier,
            final int enchantability,
            final SoundEvent equipSound,
            final float toughness,
            final float knockbackResistance,
            final String repairItemId) {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForType(final ArmorItem.Type type) {
                return switch (type) {
                    case BOOTS -> durabilityMultiplier * 13;
                    case LEGGINGS -> durabilityMultiplier * 15;
                    case CHESTPLATE -> durabilityMultiplier * 16;
                    case HELMET -> durabilityMultiplier * 11;
                    default -> durabilityMultiplier * 13;
                };
            }

            @Override
            public int getDefenseForType(final ArmorItem.Type type) {
                return defense.getOrDefault(type, 0);
            }

            @Override
            public int getEnchantmentValue() {
                return enchantability;
            }

            @Override
            public SoundEvent getEquipSound() {
                return equipSound;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("ic2port", repairItemId)));
            }

            @Override
            public String getName() {
                return "ic2port:" + name;
            }

            @Override
            public float getToughness() {
                return toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return knockbackResistance;
            }
        };
    }
}
