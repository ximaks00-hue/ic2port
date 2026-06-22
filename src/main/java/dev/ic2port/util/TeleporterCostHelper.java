package dev.ic2port.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * IC2 teleporter EU cost: {@code floor(5 * floor(weight) * (floor(distance) + 10)^0.7)}.
 * Distance uses overworld-equivalent coordinates (Nether ×8 on X/Z) for cross-dimension hops.
 */
public final class TeleporterCostHelper {

    private static final double BASE_PLAYER_WEIGHT = 1000.0D;
    private static final double ARMOR_WEIGHT = 100.0D;
    private static final double STACK_WEIGHT = 100.0D;
    private static final double COST_MULTIPLIER = 5.0D;
    private static final double DISTANCE_OFFSET = 10.0D;
    private static final double DISTANCE_EXPONENT = 0.7D;

    private TeleporterCostHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static double calculatePlayerWeight(final Player player) {
        double weight = BASE_PLAYER_WEIGHT;

        for (ItemStack armor : player.getArmorSlots()) {
            if (!armor.isEmpty()) {
                weight += ARMOR_WEIGHT;
            }
        }

        for (ItemStack stack : player.getInventory().items) {
            weight += stackWeight(stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            weight += stackWeight(stack);
        }

        return weight;
    }

    private static double stackWeight(final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }
        int max = Math.max(1, stack.getMaxStackSize());
        return STACK_WEIGHT * stack.getCount() / max;
    }

    /**
     * Euclidean distance in overworld-equivalent blocks (Nether X/Z scaled ×8).
     */
    public static double calculateDistance(
            final BlockPos source,
            final ResourceKey<Level> sourceDimension,
            final BlockPos destination,
            final ResourceKey<Level> destinationDimension) {
        double sx = toOverworldX(source.getX(), sourceDimension);
        double sy = source.getY();
        double sz = toOverworldZ(source.getZ(), sourceDimension);
        double dx = toOverworldX(destination.getX(), destinationDimension);
        double dy = destination.getY();
        double dz = toOverworldZ(destination.getZ(), destinationDimension);

        double deltaX = dx - sx;
        double deltaY = dy - sy;
        double deltaZ = dz - sz;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    private static double toOverworldX(final int coordinate, final ResourceKey<Level> dimension) {
        return dimension == Level.NETHER ? coordinate * 8.0D : coordinate;
    }

    private static double toOverworldZ(final int coordinate, final ResourceKey<Level> dimension) {
        return dimension == Level.NETHER ? coordinate * 8.0D : coordinate;
    }

    public static double calculateEuCost(final Player player, final double distance) {
        int weight = Mth.floor(calculatePlayerWeight(player));
        int flooredDistance = Mth.floor(distance);
        return Mth.floor(COST_MULTIPLIER * weight * Math.pow(flooredDistance + DISTANCE_OFFSET, DISTANCE_EXPONENT));
    }

    public static double calculateEuCost(
            final Player player,
            final BlockPos source,
            final ResourceKey<Level> sourceDimension,
            final BlockPos destination,
            final ResourceKey<Level> destinationDimension) {
        return calculateEuCost(player, calculateDistance(source, sourceDimension, destination, destinationDimension));
    }
}
