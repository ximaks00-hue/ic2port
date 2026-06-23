package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ElectricArmorHelper;
import dev.ic2port.util.MiningLaserHelper;
import dev.ic2port.util.MiningLaserHelper.MiningLaserMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HV mining laser with IC2-style firing modes (mining, scatter, explosive, tracking, etc.).
 */
public class MiningLaserItem extends ElectricItem {

    public static final String LASER_MODE_TAG = "LaserMode";
    public static final double CAPACITY = 300_000.0D;
    public static final double WEAPON_EU_PER_HIT = 1000.0D;
    public static final int ELECTRIC_ARMOR_BONUS_DAMAGE = 8;

    public MiningLaserItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.HV);
    }

    public MiningLaserMode getMode(final ItemStack stack) {
        return MiningLaserMode.fromId(stack.getOrCreateTag().getInt(LASER_MODE_TAG));
    }

    public void setMode(final ItemStack stack, final MiningLaserMode mode) {
        stack.getOrCreateTag().putInt(LASER_MODE_TAG, mode.ordinal());
    }

    public void cycleMode(final ItemStack stack) {
        setMode(stack, getMode(stack).next());
    }

    public double getModeEnergyCost(final ItemStack stack) {
        return getMode(stack).getEnergyCost();
    }

    @Nullable
    public static ItemStack findHeldLaser(final Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof MiningLaserItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof MiningLaserItem) {
            return off;
        }
        return null;
    }

    public static void notifyModeSwitch(final Player player, final ItemStack stack) {
        if (!(stack.getItem() instanceof MiningLaserItem laser)) {
            return;
        }
        MiningLaserMode mode = laser.getMode(stack);
        player.displayClientMessage(
                Component.translatable("item.ic2port.mining_laser.mode_switch", Component.translatable(mode.getTranslationKey()))
                        .withStyle(ChatFormatting.GOLD),
                true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                cycleMode(stack);
                notifyModeSwitch(player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        MiningLaserMode mode = getMode(stack);
        double energyCost = mode.getEnergyCost();
        if (getStoredEnergy(stack) < energyCost) {
            player.displayClientMessage(Component.translatable("message.ic2port.mining_laser.no_energy"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!MiningLaserHelper.fire(serverLevel, player, mode)) {
            player.displayClientMessage(Component.translatable("message.ic2port.mining_laser.no_target"), true);
            return InteractionResultHolder.fail(stack);
        }

        drawEnergy(stack, energyCost);
        player.getInventory().setChanged();
        player.getCooldowns().addCooldown(this, mode.getCooldownTicks());
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        if (!(attacker instanceof Player player)) {
            return false;
        }
        if (getStoredEnergy(stack) < WEAPON_EU_PER_HIT) {
            return false;
        }

        MiningLaserMode mode = getMode(stack);
        float damage = mode.getWeaponDamage();
        if (ElectricArmorHelper.wearsElectricArmor(target)) {
            damage += ELECTRIC_ARMOR_BONUS_DAMAGE;
        }
        target.hurt(player.damageSources().playerAttack(player), damage);
        drawEnergy(stack, WEAPON_EU_PER_HIT);
        syncHolderInventory(attacker);
        return true;
    }

    @Override
    public boolean canPerformAction(final ItemStack stack, final net.minecraftforge.common.ToolAction toolAction) {
        if (getStoredEnergy(stack) < getModeEnergyCost(stack)) {
            return false;
        }
        return toolAction == ToolActions.PICKAXE_DIG;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int stored = (int) Math.round(getStoredEnergy(stack));
        int max = (int) Math.round(getMaxEnergy());
        tooltip.add(Component.translatable("item.ic2port.mining_laser.energy", stored, max)
                .withStyle(ChatFormatting.GRAY));
        MiningLaserMode mode = getMode(stack);
        tooltip.add(Component.translatable("item.ic2port.mining_laser.mode", Component.translatable(mode.getTranslationKey()))
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.ic2port.mining_laser.energy_cost", (int) mode.getEnergyCost())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.ic2port.mining_laser.mode_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.ic2port.mining_laser.weapon_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
