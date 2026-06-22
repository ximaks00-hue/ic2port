package dev.ic2port.item;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Clears harmful potion effects when eaten (IC2 terra wart).
 */
public class TerraWartItem extends Item {

    public TerraWartItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            entity.getActiveEffects().removeIf(TerraWartItem::isHarmful);
        }
        return result;
    }

    private static boolean isHarmful(final MobEffectInstance effect) {
        return effect.getEffect().getCategory() == MobEffectCategory.HARMFUL;
    }
}
