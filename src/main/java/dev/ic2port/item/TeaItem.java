package dev.ic2port.item;

import dev.ic2port.util.TeaHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable barrel-brewed tea — quality stored in NBT via {@link TeaHelper}.
 */
public class TeaItem extends Item {

    public TeaItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyTeaEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyTeaEffects(final Player player, final ItemStack tea) {
        int quality = TeaHelper.getQuality(tea);
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 120 + quality * 40, Math.min(1, quality / 3)));
        if (quality >= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
        }
        if (quality <= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        }
    }
}
