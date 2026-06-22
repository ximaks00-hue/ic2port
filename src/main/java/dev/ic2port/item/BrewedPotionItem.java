package dev.ic2port.item;

import dev.ic2port.brewing.PotionQuality;
import dev.ic2port.util.PotionHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable barrel-brewed potion — quality stored in NBT via {@link PotionHelper}.
 */
public class BrewedPotionItem extends Item {

    public BrewedPotionItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyPotionEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyPotionEffects(final Player player, final ItemStack potion) {
        PotionQuality quality = PotionHelper.getQuality(potion);
        switch (quality) {
            case BAD -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 0));
            case RAW -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            case UNREFINED -> player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
            case IMPURE -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
            case REDUCED -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0));
            }
            case PURE -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 0));
            }
            case CONCENTRATED -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
            }
        }
    }
}
