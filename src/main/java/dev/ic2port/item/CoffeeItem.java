package dev.ic2port.item;

import dev.ic2port.util.CoffeeHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable barrel-brewed coffee — strength stored in NBT via {@link CoffeeHelper}.
 */
public class CoffeeItem extends Item {

    public CoffeeItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyCoffeeEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyCoffeeEffects(final Player player, final ItemStack coffee) {
        int strength = CoffeeHelper.getStrength(coffee);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160 + strength * 30, Math.min(1, strength / 3)));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 120 + strength * 20, 0));
        if (strength >= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0));
        }
        if (strength <= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        }
    }
}
