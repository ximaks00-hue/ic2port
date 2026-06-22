package dev.ic2port.item;

import dev.ic2port.brewing.BrewQuality;
import dev.ic2port.brewing.PotionQuality;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.BeerHelper;
import dev.ic2port.util.FoodCanningHelper;
import dev.ic2port.util.PotionHelper;
import dev.ic2port.util.RumHelper;
import dev.ic2port.util.WhiskyHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Canned food — restores hunger and returns an empty tin can when eaten.
 */
public class FilledTinCanItem extends Item {

    public FilledTinCanItem(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack stack, final Level level, final LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyStoredFoodEffects(player, stack);
            if (!player.getAbilities().instabuild) {
                if (!player.getInventory().add(new ItemStack(ItemRegistry.TIN_CAN.get()))) {
                    player.drop(new ItemStack(ItemRegistry.TIN_CAN.get()), false);
                }
            }
        }
        return result;
    }

    /**
     * Applies canned food effects without requiring a use animation (auto-feeder).
     */
    public static void feedPlayer(final Player player, final ItemStack can) {
        if (!can.is(ItemRegistry.FILLED_TIN_CAN.get()) || can.isEmpty()) {
            return;
        }
        applyStoredFoodEffects(player, can);
        can.shrink(1);
        if (!player.getAbilities().instabuild) {
            if (!player.getInventory().add(new ItemStack(ItemRegistry.TIN_CAN.get()))) {
                player.drop(new ItemStack(ItemRegistry.TIN_CAN.get()), false);
            }
        }
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        Item stored = FoodCanningHelper.getStoredFoodItem(stack);
        if (stored != null) {
            tooltip.add(Component.translatable(
                    "item.ic2port.filled_tin_can.stored",
                    new ItemStack(stored).getHoverName()));
        }
    }

    private static void applyStoredFoodEffects(final Player player, final ItemStack can) {
        ItemStack food = FoodCanningHelper.recreateStoredFood(can);
        if (food.isEmpty()) {
            player.getFoodData().eat(1, 0.3F);
            return;
        }

        FoodProperties properties = food.getItem().getFoodProperties();
        int nutrition = properties == null ? 1 : Math.max(1, properties.getNutrition());
        float saturation = properties == null ? 0.3F : properties.getSaturationModifier();
        player.getFoodData().eat(Math.min(1, nutrition), Math.min(0.6F, saturation));

        if (food.is(ItemRegistry.BEER.get())) {
            applyBeerEffects(player, food);
        } else if (food.is(ItemRegistry.RUM.get())) {
            applyRumEffects(player, food);
        } else if (food.is(ItemRegistry.WHISKY.get())) {
            applyWhiskyEffects(player, food);
        } else if (food.is(ItemRegistry.BREWED_POTION.get())) {
            applyBrewedPotionEffects(player, food);
        } else if (food.is(Items.ROTTEN_FLESH)) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
        } else if (food.is(Items.SPIDER_EYE)) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        } else if (food.is(Items.PUFFERFISH)) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
        }
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

    private static void applyRumEffects(final Player player, final ItemStack rum) {
        int strength = RumHelper.getStrength(rum);
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED, 200 + strength * 40, Math.min(1, strength / 3)));
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

    private static void applyBrewedPotionEffects(final Player player, final ItemStack potion) {
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
