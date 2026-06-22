package dev.ic2port.block;

import dev.ic2port.setup.ModEffects;
import dev.ic2port.item.HazmatArmorItem;
import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Irradiated soil left behind by reactor meltdowns.
 */
public class ContaminatedSoilBlock extends Block {

    private static final int RADIATION_DURATION_TICKS = 120;
    private static final int RADIATION_AMPLIFIER = 1;

    public ContaminatedSoilBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(0.6F)
                .sound(net.minecraft.world.level.block.SoundType.GRAVEL));
    }

    @Override
    public void stepOn(final Level level, final BlockPos pos, final BlockState state, final Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            super.stepOn(level, pos, state, entity);
            return;
        }

        if (living instanceof net.minecraft.world.entity.player.Player player
                && (HazmatArmorItem.hasFullSet(player)
                || NanoSuitItem.hasFullSet(player)
                || QuantumSuitItem.hasFullSet(player))) {
            super.stepOn(level, pos, state, entity);
            return;
        }

        living.addEffect(new MobEffectInstance(
                ModEffects.RADIATION.get(),
                RADIATION_DURATION_TICKS,
                RADIATION_AMPLIFIER,
                false,
                true,
                true));
        super.stepOn(level, pos, state, entity);
    }
}
