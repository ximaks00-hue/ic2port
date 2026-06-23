package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ElectricArmorHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HV nano saber — toggleable energy blade effective against nano and quantum armor.
 */
public class NanoSaberItem extends SwordItem {

    public static final String ACTIVE_TAG = "Active";
    public static final double CAPACITY = 200_000.0D;
    public static final double EU_PER_HIT = 1000.0D;
    public static final float ACTIVE_DAMAGE = 12.0F;
    public static final float ELECTRIC_ARMOR_BONUS = 8.0F;

    private static final String ENERGY_TAG = "Energy";

    public NanoSaberItem(final Properties properties) {
        super(Tiers.DIAMOND, 3, -2.4F, properties.stacksTo(1).fireResistant());
    }

    public boolean isActive(final ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(ACTIVE_TAG);
    }

    public void setActive(final ItemStack stack, final boolean active) {
        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
    }

    public int getEnergyTier() {
        return EnergyTier.HV;
    }

    public double getMaxEnergy() {
        return CAPACITY;
    }

    public double getStoredEnergy(final ItemStack stack) {
        if (!stack.hasTag()) {
            return 0.0D;
        }
        return Math.min(CAPACITY, Math.max(0.0D, stack.getTag().getDouble(ENERGY_TAG)));
    }

    public void setStoredEnergy(final ItemStack stack, final double energy) {
        stack.getOrCreateTag().putDouble(ENERGY_TAG, Math.max(0.0D, Math.min(CAPACITY, energy)));
    }

    public double charge(final ItemStack stack, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double space = CAPACITY - getStoredEnergy(stack);
        double accepted = Math.min(amount, space);
        if (accepted > 0.0D) {
            setStoredEnergy(stack, getStoredEnergy(stack) + accepted);
        }
        return amount - accepted;
    }

    public double drawEnergy(final ItemStack stack, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        double stored = getStoredEnergy(stack);
        double drawn = Math.min(amount, stored);
        if (drawn > 0.0D) {
            setStoredEnergy(stack, stored - drawn);
        }
        return drawn;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            boolean active = !isActive(stack);
            setActive(stack, active);
            if (active) {
                player.displayClientMessage(
                        Component.translatable("item.ic2port.nano_saber.enable").withStyle(ChatFormatting.GREEN),
                        true);
            } else {
                player.displayClientMessage(
                        Component.translatable("item.ic2port.nano_saber.disable").withStyle(ChatFormatting.GRAY),
                        true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        if (!isActive(stack)) {
            return true;
        }
        if (getStoredEnergy(stack) < EU_PER_HIT) {
            return false;
        }
        drawEnergy(stack, EU_PER_HIT);
        ElectricItem.syncHolderInventory(attacker);
        return true;
    }

    /**
     * @return total damage for an active strike (used by forge event handler)
     */
    public static float resolveActiveDamage(final LivingEntity target) {
        float damage = ACTIVE_DAMAGE;
        if (ElectricArmorHelper.wearsElectricArmor(target)) {
            damage += ELECTRIC_ARMOR_BONUS;
        }
        return damage;
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return getStoredEnergy(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return Math.round(13.0F * (float) (getStoredEnergy(stack) / CAPACITY));
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        float ratio = (float) (getStoredEnergy(stack) / CAPACITY);
        return net.minecraft.util.Mth.hsvToRgb(0.75F, 1.0F, 0.5F + ratio * 0.5F);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int stored = (int) Math.round(getStoredEnergy(stack));
        tooltip.add(Component.translatable("item.ic2port.nano_saber.energy", stored, (int) CAPACITY)
                .withStyle(ChatFormatting.GRAY));
        if (isActive(stack)) {
            tooltip.add(Component.translatable("item.ic2port.nano_saber.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("item.ic2port.nano_saber.inactive")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("item.ic2port.nano_saber.counter_hint")
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.ic2port.nano_saber.toggle_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
