package dev.ic2port.util;

import dev.ic2port.api.reactor.IReactorFuel;
import dev.ic2port.blockentity.NuclearReactorBlockEntity;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.setup.ModEffects;
import dev.ic2port.item.HazmatArmorItem;
import dev.ic2port.item.NanoSuitItem;
import dev.ic2port.item.QuantumSuitItem;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Critical overheat effects and meltdown aftermath.
 */
public final class ReactorMeltdownHelper {

    private static final int IGNITE_RADIUS = 3;
    private static final int RADIATION_AMPLIFIER = 4;
    private static final int RADIATION_DURATION_TICKS = 100;

    private ReactorMeltdownHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static double heatWarningRatio() {
        return ModConfig.REACTOR_HEAT_WARNING_RATIO.get();
    }

    private static double heatRadiationRatio() {
        return ModConfig.REACTOR_HEAT_RADIATION_RATIO.get();
    }

    private static int radiationRadius() {
        return ModConfig.REACTOR_RADIATION_RADIUS.get();
    }

    public static void applyOverheatEffects(
            final Level level,
            final BlockPos reactorPos,
            final double heat,
            final double maxHeat) {
        if (level.isClientSide) {
            return;
        }

        double ratio = heat / maxHeat;
        if (ratio > heatWarningRatio()) {
            igniteNearbyBlocks(level, reactorPos, IGNITE_RADIUS);
        }
        if (ratio > heatRadiationRatio()) {
            irradiateNearbyPlayers(level, reactorPos, radiationRadius());
        }
    }

    public static float explosionPowerForInventory(final NuclearReactorBlockEntity reactor) {
        int remainingFuel = countRemainingFuel(reactor);
        float base = 4.0F;
        float bonus = Mth.clamp(remainingFuel / 25_000.0F, 0.0F, 8.0F);
        return base + bonus;
    }

    public static int contaminationRadiusForInventory(final NuclearReactorBlockEntity reactor) {
        int remainingFuel = countRemainingFuel(reactor);
        return Mth.clamp(5 + remainingFuel / 8_000, 5, 15);
    }

    public static void contaminateArea(final Level level, final BlockPos center, final int radius) {
        BlockPos.betweenClosedStream(
                center.offset(-radius, -2, -radius),
                center.offset(radius, 2, radius)
        ).forEach(pos -> {
            if (center.distSqr(pos) > radius * radius) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.GRASS_BLOCK)
                    || state.is(Blocks.DIRT)
                    || state.is(Blocks.COARSE_DIRT)
                    || state.is(Blocks.PODZOL)
                    || state.is(Blocks.ROOTED_DIRT)
                    || state.is(Blocks.MYCELIUM)) {
                level.setBlock(pos, BlockRegistry.CONTAMINATED_SOIL.get().defaultBlockState(), 3);
            }
        });
    }

    private static int countRemainingFuel(final NuclearReactorBlockEntity reactor) {
        int total = 0;
        for (int slot = 0; slot < NuclearReactorBlockEntity.SLOT_COUNT; slot++) {
            int x = NuclearReactorBlockEntity.slotToX(slot);
            if (!reactor.isColumnEnabled(x)) {
                continue;
            }
            ItemStack stack = reactor.getStack(
                    x,
                    NuclearReactorBlockEntity.slotToY(slot));
            if (stack.getItem() instanceof IReactorFuel fuel && !fuel.isDepleted(stack)) {
                total += fuel.getMaxDepletion() - fuel.getDepletion(stack);
            }
        }
        return total;
    }

    private static void igniteNearbyBlocks(final Level level, final BlockPos center, final int radius) {
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        BlockPos randomPos = center.offset(
                level.random.nextInt(radius * 2 + 1) - radius,
                level.random.nextInt(radius * 2 + 1) - radius,
                level.random.nextInt(radius * 2 + 1) - radius);
        BlockPos firePos = randomPos.above();
        if (level.isEmptyBlock(firePos) && Blocks.FIRE.defaultBlockState().canSurvive(level, firePos)) {
            level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
        }
    }

    private static void irradiateNearbyPlayers(final Level level, final BlockPos center, final int radius) {
        double radiusSq = radius * radius;
        for (Player player : level.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(center).inflate(radius))) {
            if (player.distanceToSqr(center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D) > radiusSq) {
                continue;
            }
            if (HazmatArmorItem.hasFullSet(player) || NanoSuitItem.hasFullSet(player) || QuantumSuitItem.hasFullSet(player)) {
                continue;
            }
            player.addEffect(new MobEffectInstance(
                    ModEffects.RADIATION.get(),
                    RADIATION_DURATION_TICKS,
                    RADIATION_AMPLIFIER,
                    false,
                    true,
                    true));
        }
    }
}
