package dev.ic2port.setup;

import dev.ic2port.Reference;
import dev.ic2port.item.ElectricJetpackItem;
import dev.ic2port.item.QuantumSuitItem;
import dev.ic2port.util.PlayerInputHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class QuantumSuitForgeEvents {

    private static final UUID LEGGINGS_SPEED_MODIFIER_ID =
            UUID.fromString("8c4f2a10-6b3d-4e91-9f2c-1d7e5a8b0c42");

    private QuantumSuitForgeEvents() {
        throw new UnsupportedOperationException("Utility class");
    }

    @SubscribeEvent
    public static void onLivingHurt(final LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (!QuantumSuitItem.hasFullSet(player)) {
            return;
        }

        float remaining;
        if (event.getSource().is(DamageTypes.FALL)) {
            remaining = QuantumSuitItem.absorbFallDamage(player, event.getAmount());
        } else {
            remaining = QuantumSuitItem.absorbDamage(player, event.getAmount());
        }

        if (remaining < event.getAmount()) {
            event.setAmount(remaining);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isServer()) {
            return;
        }

        Player player = event.player;
        tickHelmet(player);
        tickChestplate(player);
        tickLeggings(player);
        tickBoots(player);
    }

    private static void tickHelmet(final Player player) {
        ItemStack helmet = QuantumSuitItem.getPiece(player, QuantumSuitItem.Type.HELMET);
        if (helmet == null) {
            return;
        }
        QuantumSuitItem quantum = (QuantumSuitItem) helmet.getItem();

        if (quantum.isNightVisionEnabled(helmet)
                && quantum.getStoredEnergy(helmet) >= QuantumSuitItem.EU_NIGHT_VISION_PER_TICK) {
            if (QuantumSuitItem.drainEnergyFromSlot(player, EquipmentSlot.HEAD, QuantumSuitItem.EU_NIGHT_VISION_PER_TICK)
                    >= QuantumSuitItem.EU_NIGHT_VISION_PER_TICK) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        220,
                        0,
                        false,
                        false,
                        true));
            }
        }

        if (player.isUnderWater() && player.getAirSupply() < player.getMaxAirSupply()) {
            double cost = QuantumSuitItem.EU_AIR_PER_POINT;
            if (quantum.getStoredEnergy(helmet) >= cost
                    && QuantumSuitItem.drainEnergyFromSlot(player, EquipmentSlot.HEAD, cost) >= cost) {
                player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 2));
            }
        }
    }

    private static void tickChestplate(final Player player) {
        ItemStack chest = QuantumSuitItem.getPiece(player, QuantumSuitItem.Type.CHESTPLATE);
        if (chest == null) {
            return;
        }
        QuantumSuitItem quantum = (QuantumSuitItem) chest.getItem();

        if (quantum.getStoredEnergy(chest) < QuantumSuitItem.JETPACK_MIN_ENERGY) {
            return;
        }
        if (player.onGround() || player.isInWater() || player.isPassenger()) {
            return;
        }

        ElectricJetpackItem.JetpackMode mode = quantum.getJetpackMode(chest);
        Vec3 motion = player.getDeltaMovement();

        if (mode == ElectricJetpackItem.JetpackMode.NORMAL) {
            if (!isJumping(player)) {
                return;
            }
            if (quantum.drawEnergy(chest, QuantumSuitItem.JETPACK_NORMAL_COST) < QuantumSuitItem.JETPACK_NORMAL_COST) {
                return;
            }
            player.setDeltaMovement(motion.x, 0.45D, motion.z);
            spawnJetpackSmoke(player);
            return;
        }

        if (quantum.drawEnergy(chest, QuantumSuitItem.JETPACK_HOVER_COST) < QuantumSuitItem.JETPACK_HOVER_COST) {
            return;
        }

        if (isJumping(player)) {
            player.setDeltaMovement(motion.x, 0.12D, motion.z);
        } else if (motion.y < -0.15D) {
            player.setDeltaMovement(motion.x, -0.15D, motion.z);
            player.fallDistance = 0.0F;
        }
    }

    private static void tickLeggings(final Player player) {
        ItemStack leggings = QuantumSuitItem.getPiece(player, QuantumSuitItem.Type.LEGGINGS);
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        if (leggings == null) {
            movementSpeed.removeModifier(LEGGINGS_SPEED_MODIFIER_ID);
            return;
        }

        QuantumSuitItem quantum = (QuantumSuitItem) leggings.getItem();
        final boolean canBoost = quantum.getStoredEnergy(leggings) >= QuantumSuitItem.EU_SPEED_PER_TICK
                && player.getDeltaMovement().horizontalDistanceSqr() > 0.001D
                && !player.isShiftKeyDown();

        if (canBoost) {
            if (movementSpeed.getModifier(LEGGINGS_SPEED_MODIFIER_ID) == null) {
                movementSpeed.addTransientModifier(new AttributeModifier(
                        LEGGINGS_SPEED_MODIFIER_ID,
                        "ic2port.quantum_leggings_speed",
                        0.35D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            QuantumSuitItem.drainEnergyFromSlot(player, EquipmentSlot.LEGS, QuantumSuitItem.EU_SPEED_PER_TICK);
        } else {
            movementSpeed.removeModifier(LEGGINGS_SPEED_MODIFIER_ID);
        }
    }

    private static void tickBoots(final Player player) {
        ItemStack boots = QuantumSuitItem.getPiece(player, QuantumSuitItem.Type.BOOTS);
        if (boots == null) {
            return;
        }
        QuantumSuitItem quantum = (QuantumSuitItem) boots.getItem();

        if (player.onGround() && isJumping(player) && quantum.getStoredEnergy(boots) >= QuantumSuitItem.EU_JUMP_BOOST) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.y <= 0.1D) {
                quantum.drawEnergy(boots, QuantumSuitItem.EU_JUMP_BOOST);
                player.setDeltaMovement(motion.x, 0.55D, motion.z);
            }
        }
    }

    private static boolean isJumping(final Player player) {
        return PlayerInputHelper.isJumping(player);
    }

    private static void spawnJetpackSmoke(final Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.2D,
                player.getZ(),
                4,
                0.1D,
                0.05D,
                0.1D,
                0.01D);
    }
}
