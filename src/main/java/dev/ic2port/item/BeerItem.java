package dev.ic2port.item;

import dev.ic2port.brewing.BrewQuality;
import dev.ic2port.util.BeerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Drinkable brewed beer — stats stored in NBT via {@link BeerHelper}.
 */
public class BeerItem extends Item {

    public BeerItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyBeerEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                    player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
                }
            }
        }
        return result;
    }

    private static void applyBeerEffects(final Player player, final ItemStack beer) {
        BrewQuality quality = BeerHelper.getQuality(beer);
        int alcohol = BeerHelper.getAlcohol(beer);
        switch (quality) {
            case BAD -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            case YOUNGSTER -> {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0));
            }
            case BREW -> player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
            case BEER -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
            case ALE -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                if (alcohol >= 4) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                }
            }
            case DRAGONBLOOD -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 0));
            }
        }
    }
}
