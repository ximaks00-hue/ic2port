package dev.ic2port.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Server-side block interaction logic for {@link dev.ic2port.item.MiningLaserItem} modes.
 */
public final class MiningLaserHelper {

    private MiningLaserHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean fire(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        return switch (mode) {
            case MINING -> breakRaycastBlock(level, player, mode);
            case LOW_FOCUS -> breakRaycastBlock(level, player, mode);
            case LONG_RANGE -> breakRaycastBlock(level, player, mode);
            case HORIZONTAL -> breakHorizontalLine(level, player, mode);
            case SCATTER -> breakScatter(level, player, mode);
            case EXPLOSIVE -> explodeAtTarget(level, player, mode);
            case SUPER_HEAT -> superHeatTarget(level, player, mode);
            case TRACKING -> breakTrackingOre(level, player, mode);
        };
    }

    private static boolean breakRaycastBlock(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockHitResult hit = raycast(level, player, mode.getRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        return breakBlock(level, player, hit.getBlockPos(), mode.getEnergyCost());
    }

    private static boolean breakHorizontalLine(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockHitResult hit = raycast(level, player, mode.getRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        Vec3 look = player.getLookAngle();
        Direction horizontal = Direction.getNearest(look.x, 0.0D, look.z);
        BlockPos start = hit.getBlockPos();
        boolean brokeAny = false;
        for (int step = 0; step < mode.getHorizontalLength(); step++) {
            BlockPos pos = start.relative(horizontal, step);
            if (breakBlock(level, player, pos, mode.getEnergyCost())) {
                brokeAny = true;
            }
        }
        return brokeAny;
    }

    private static boolean breakScatter(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockHitResult hit = raycast(level, player, mode.getRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos center = hit.getBlockPos();
        Direction face = hit.getDirection();
        boolean brokeAny = false;
        for (int u = -1; u <= 1; u++) {
            for (int v = -1; v <= 1; v++) {
                BlockPos offset = offsetOnFacePlane(center, face, u, v);
                if (breakBlock(level, player, offset, mode.getEnergyCost())) {
                    brokeAny = true;
                }
            }
        }
        return brokeAny;
    }

    private static boolean explodeAtTarget(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockHitResult hit = raycast(level, player, mode.getRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        }

        Vec3 center = Vec3.atCenterOf(hit.getBlockPos());
        level.explode(player, center.x, center.y, center.z, mode.getExplosionPower(), Level.ExplosionInteraction.TNT);
        return true;
    }

    private static boolean superHeatTarget(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockHitResult hit = raycast(level, player, mode.getRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!canMineBlock(level, player, pos, state)) {
            return false;
        }

        ItemStack blockStack = new ItemStack(state.getBlock().asItem());
        if (blockStack.isEmpty()) {
            return breakBlock(level, player, pos, mode.getEnergyCost());
        }

        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SimpleContainer(blockStack), level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .filter(result -> !result.isEmpty())
                .map(result -> {
                    level.destroyBlock(pos, false, player);
                    Block.popResource(level, pos, result.copy());
                    return true;
                })
                .orElseGet(() -> breakBlock(level, player, pos, mode.getEnergyCost()));
    }

    private static boolean breakTrackingOre(
            final ServerLevel level,
            final Player player,
            final MiningLaserMode mode) {
        BlockPos origin = player.blockPosition();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        List<BlockPos> candidates = new ArrayList<>();
        BlockPos.betweenClosedStream(
                        origin.offset(-mode.getRange(), -mode.getRange(), -mode.getRange()),
                        origin.offset(mode.getRange(), mode.getRange(), mode.getRange()))
                .filter(pos -> isOreBlock(level.getBlockState(pos)))
                .forEach(candidates::add);

        if (candidates.isEmpty()) {
            return breakRaycastBlock(level, player, mode);
        }

        BlockPos best = candidates.stream()
                .min(Comparator.comparingDouble(pos -> angularDistance(eye, look, Vec3.atCenterOf(pos))))
                .orElse(null);
        if (best == null) {
            return false;
        }
        return breakBlock(level, player, best, mode.getEnergyCost());
    }

    private static double angularDistance(final Vec3 eye, final Vec3 look, final Vec3 target) {
        Vec3 toTarget = target.subtract(eye).normalize();
        return 1.0D - look.dot(toTarget);
    }

    private static boolean isOreBlock(final BlockState state) {
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES);
    }

    public static boolean breakBlock(
            final ServerLevel level,
            final Player player,
            final BlockPos pos,
            final double ignoredEnergyCostMarker) {
        BlockState state = level.getBlockState(pos);
        if (!canMineBlock(level, player, pos, state)) {
            return false;
        }
        return level.destroyBlock(pos, true, player);
    }

    private static boolean canMineBlock(
            final Level level,
            final Player player,
            final BlockPos pos,
            final BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            return false;
        }
        return state.canSurvive(level, pos) && state.getBlock().canHarvestBlock(state, level, pos, player);
    }

    private static BlockPos offsetOnFacePlane(
            final BlockPos center,
            final Direction face,
            final int u,
            final int v) {
        return switch (face.getAxis()) {
            case X -> center.offset(0, u, v);
            case Y -> center.offset(u, 0, v);
            case Z -> center.offset(u, v, 0);
        };
    }

    private static BlockHitResult raycast(final Level level, final Player player, final int range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    /**
     * Laser firing modes aligned with IC2 Classic behaviour.
     */
    public enum MiningLaserMode {
        MINING(2000.0D, 32, 10, 1, 0.0F, 5),
        LOW_FOCUS(500.0D, 8, 5, 1, 0.0F, 3),
        LONG_RANGE(5000.0D, 64, 20, 1, 0.0F, 8),
        HORIZONTAL(1000.0D, 32, 12, 5, 0.0F, 6),
        SCATTER(125.0D, 32, 15, 1, 0.0F, 7),
        EXPLOSIVE(5000.0D, 32, 30, 1, 2.5F, 10),
        SUPER_HEAT(2500.0D, 32, 12, 1, 0.0F, 6),
        TRACKING(3000.0D, 16, 12, 1, 0.0F, 6);

        private final double energyCost;
        private final int range;
        private final int cooldownTicks;
        private final int horizontalLength;
        private final float explosionPower;
        private final int weaponDamage;

        MiningLaserMode(
                final double energyCost,
                final int range,
                final int cooldownTicks,
                final int horizontalLength,
                final float explosionPower,
                final int weaponDamage) {
            this.energyCost = energyCost;
            this.range = range;
            this.cooldownTicks = cooldownTicks;
            this.horizontalLength = horizontalLength;
            this.explosionPower = explosionPower;
            this.weaponDamage = weaponDamage;
        }

        public double getEnergyCost() {
            return energyCost;
        }

        public int getRange() {
            return range;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }

        public int getHorizontalLength() {
            return horizontalLength;
        }

        public float getExplosionPower() {
            return explosionPower;
        }

        public int getWeaponDamage() {
            return weaponDamage;
        }

        public String getTranslationKey() {
            return "item.ic2port.mining_laser.mode." + name().toLowerCase();
        }

        public MiningLaserMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static MiningLaserMode fromId(final int id) {
            MiningLaserMode[] modes = values();
            if (id < 0 || id >= modes.length) {
                return MINING;
            }
            return modes[id];
        }
    }
}
