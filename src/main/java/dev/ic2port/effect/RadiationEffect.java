package dev.ic2port.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Harmful effect applied when carrying unshielded radioactive materials.
 */
public class RadiationEffect extends MobEffect {

    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 0x6BFF2E);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyEffectTick(final LivingEntity entity, final int amplifier) {
        if (entity instanceof Player player) {
            player.hurt(player.damageSources().magic(), 2.0F + amplifier);
        }
    }
}
