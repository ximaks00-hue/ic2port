package dev.ic2port.item;

import dev.ic2port.util.RumHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable barrel-brewed rum — strength stored in NBT via {@link RumHelper}.
 */
public class RumItem extends Item {

    public RumItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyRumEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyRumEffects(final Player player, final ItemStack rum) {
        int strength = RumHelper.getStrength(rum);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200 + strength * 40, Math.min(1, strength / 3)));
        if (strength >= 4) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0));
        }
        if (strength >= 5) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
        }
        if (strength <= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        }
    }
}
