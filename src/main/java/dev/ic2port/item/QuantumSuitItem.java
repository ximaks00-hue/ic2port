package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.ArmorSetHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Quantum armor — absorbs damage by consuming stored EU and provides piece-specific abilities.
 */
public class QuantumSuitItem extends ElectricArmorItem {

    public static final String NIGHT_VISION_TAG = "NightVision";
    public static final String JETPACK_MODE_TAG = "JetpackMode";

    public static final double EU_PER_DAMAGE = 800.0D;
    public static final double EU_PER_FALL_DAMAGE = 400.0D;
    public static final double EU_NIGHT_VISION_PER_TICK = 64.0D;
    public static final double EU_AIR_PER_POINT = 32.0D;
    public static final double EU_SPEED_PER_TICK = 24.0D;
    public static final double EU_JUMP_BOOST = 120.0D;
    public static final double JETPACK_MIN_ENERGY = 200.0D;
    public static final int JETPACK_NORMAL_COST = 200;
    public static final int JETPACK_HOVER_COST = 80;

    private final double capacity;

    public QuantumSuitItem(final Type type, final Properties properties, final double capacity) {
        super(ArmorMaterials.NETHERITE, type, properties, capacity, EnergyTier.HV);
        this.capacity = capacity;
    }

    public static double getCapacityFor(final Type type) {
        return switch (type) {
            case HELMET -> 500_000.0D;
            case CHESTPLATE -> 2_000_000.0D;
            case LEGGINGS -> 1_000_000.0D;
            case BOOTS -> 500_000.0D;
            default -> 500_000.0D;
        };
    }

    public static boolean hasFullSet(final Player player) {
        return ArmorSetHelper.hasFullTypedSet(player, QuantumSuitItem.class);
    }

    @Nullable
    public static ItemStack getPiece(final Player player, final Type type) {
        EquipmentSlot slot = switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
            default -> null;
        };
        if (slot == null) {
            return null;
        }
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.getItem() instanceof QuantumSuitItem quantum && quantum.getType() == type) {
            return stack;
        }
        return null;
    }

    public static double getTotalStoredEnergy(final Player player) {
        double total = 0.0D;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof QuantumSuitItem quantum) {
                total += quantum.getStoredEnergy(stack);
            }
        }
        return total;
    }

    public static float absorbDamage(final Player player, final float damage) {
        return absorbDamage(player, damage, EU_PER_DAMAGE, null);
    }

    public static float absorbFallDamage(final Player player, final float damage) {
        return absorbDamage(player, damage, EU_PER_FALL_DAMAGE, EquipmentSlot.FEET);
    }

    private static float absorbDamage(
            final Player player,
            final float damage,
            final double euPerDamage,
            final @Nullable EquipmentSlot preferredSlot) {
        if (damage <= 0.0F || euPerDamage <= 0.0D) {
            return damage;
        }

        double energyNeeded = damage * euPerDamage;
        double energyUsed = 0.0D;

        if (preferredSlot != null) {
            energyUsed += drainEnergyFromSlot(player, preferredSlot, energyNeeded);
        }
        if (energyUsed < energyNeeded) {
            energyUsed += drainEnergy(player, energyNeeded - energyUsed, preferredSlot);
        }

        double absorbedDamage = energyUsed / euPerDamage;
        return (float) Math.max(0.0D, damage - absorbedDamage);
    }

    public static double drainEnergy(final Player player, final double amount) {
        return drainEnergy(player, amount, null);
    }

    private static double drainEnergy(
            final Player player,
            final double amount,
            final @Nullable EquipmentSlot skipSlot) {
        double remaining = amount;
        double drained = 0.0D;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR || remaining <= 0.0D || slot == skipSlot) {
                continue;
            }
            double drawn = drainEnergyFromSlot(player, slot, remaining);
            drained += drawn;
            remaining -= drawn;
        }
        return drained;
    }

    public static double drainEnergyFromSlot(final Player player, final EquipmentSlot slot, final double amount) {
        if (amount <= 0.0D) {
            return 0.0D;
        }
        ItemStack stack = player.getItemBySlot(slot);
        if (!(stack.getItem() instanceof QuantumSuitItem quantum)) {
            return 0.0D;
        }
        return quantum.drawEnergy(stack, amount);
    }

    public boolean isNightVisionEnabled(final ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NIGHT_VISION_TAG);
    }

    public boolean toggleNightVision(final ItemStack stack) {
        boolean enabled = !isNightVisionEnabled(stack);
        stack.getOrCreateTag().putBoolean(NIGHT_VISION_TAG, enabled);
        return enabled;
    }

    public ElectricJetpackItem.JetpackMode getJetpackMode(final ItemStack stack) {
        return ElectricJetpackItem.JetpackMode.fromId(stack.getOrCreateTag().getInt(JETPACK_MODE_TAG));
    }

    public void setJetpackMode(final ItemStack stack, final ElectricJetpackItem.JetpackMode mode) {
        stack.getOrCreateTag().putInt(JETPACK_MODE_TAG, mode.ordinal());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching()) {
            return InteractionResultHolder.pass(stack);
        }

        if (getType() == Type.CHESTPLATE) {
            if (!level.isClientSide) {
                ElectricJetpackItem.JetpackMode next = getJetpackMode(stack).next();
                setJetpackMode(stack, next);
                player.displayClientMessage(
                        Component.empty()
                                .append(Component.translatable("item.ic2port.quantum_chestplate.jetpack_switch_prefix")
                                        .withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(next.getTranslationKey())
                                        .withStyle(next.getChatColor())),
                        true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (getType() == Type.HELMET) {
            if (!level.isClientSide) {
                boolean enabled = toggleNightVision(stack);
                player.displayClientMessage(
                        Component.translatable(
                                        enabled
                                                ? "item.ic2port.quantum_helmet.night_vision.on"
                                                : "item.ic2port.quantum_helmet.night_vision.off")
                                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY),
                        true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable(
                        "item.ic2port.quantum_suit.energy",
                        (int) Math.round(getStoredEnergy(stack)),
                        (int) Math.round(capacity))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.quantum_suit.absorb_hint", (int) EU_PER_DAMAGE)
                .withStyle(ChatFormatting.DARK_AQUA));

        switch (getType()) {
            case HELMET -> {
                tooltip.add(Component.translatable(
                                "item.ic2port.quantum_helmet.night_vision",
                                Component.translatable(isNightVisionEnabled(stack)
                                        ? "item.ic2port.quantum_helmet.night_vision.enabled"
                                        : "item.ic2port.quantum_helmet.night_vision.disabled"))
                        .withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.add(Component.translatable("item.ic2port.quantum_helmet.abilities_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            case CHESTPLATE -> {
                ElectricJetpackItem.JetpackMode mode = getJetpackMode(stack);
                tooltip.add(Component.translatable(
                                "item.ic2port.quantum_chestplate.jetpack_mode",
                                Component.translatable(mode.getTranslationKey()))
                        .withStyle(mode.getChatColor()));
                tooltip.add(Component.translatable("item.ic2port.quantum_chestplate.jetpack_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            case LEGGINGS -> tooltip.add(Component.translatable("item.ic2port.quantum_leggings.speed_hint")
                    .withStyle(ChatFormatting.DARK_GREEN));
            case BOOTS -> tooltip.add(Component.translatable("item.ic2port.quantum_boots.fall_hint", (int) EU_PER_FALL_DAMAGE)
                    .withStyle(ChatFormatting.DARK_BLUE));
            default -> {
            }
        }
    }
}
