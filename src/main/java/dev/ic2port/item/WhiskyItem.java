package dev.ic2port.item;

import dev.ic2port.util.WhiskyHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable barrel-aged whisky — age stored in NBT via {@link WhiskyHelper}.
 */
public class WhiskyItem extends Item {

    public WhiskyItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyWhiskyEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyWhiskyEffects(final Player player, final ItemStack whisky) {
        int years = WhiskyHelper.getYears(whisky);
        int duration = 100 + years * 20;
        int amplifier = Math.min(2, years / 15);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
        if (years >= 10) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration / 2, 0));
        }
        if (years >= 25) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, Math.min(200, years * 4), 0));
        }
        if (years <= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0));
        }
    }
}
