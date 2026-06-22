package dev.ic2port.util;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.crop.CropRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * IC2-style cross-breeding between adjacent crop sticks.
 */
public final class CropBreedingHelper {

    private static final float BREED_CHANCE_PER_TICK = 0.04F;
    private static final int MIN_PARENTS = 2;

    private CropBreedingHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void tryBreedEmptyCenter(final CropSticksBlockEntity center, final RandomSource random) {
        if (center.getCrop() != null || center.getLevel() == null) {
            return;
        }

        List<CropSticksBlockEntity> parents = findEligibleParents(center.getLevel(), center.getBlockPos(), random);
        if (parents.size() < MIN_PARENTS) {
            return;
        }
        if (random.nextFloat() > BREED_CHANCE_PER_TICK) {
            return;
        }

        ICrop childCrop = selectChildCrop(center, parents, random);
        int growth = blendStat(parents, CropSticksBlockEntity::getGrowthStat, random);
        int gain = blendStat(parents, CropSticksBlockEntity::getGainStat, random);
        int resistance = blendStat(parents, CropSticksBlockEntity::getResistanceStat, random);
        center.applyBreedingResult(childCrop, growth, gain, resistance);
    }

    private static List<CropSticksBlockEntity> findEligibleParents(
            final Level level,
            final BlockPos center,
            final RandomSource random) {
        List<CropSticksBlockEntity> parents = new ArrayList<>(4);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockEntity blockEntity = level.getBlockEntity(center.relative(direction));
            if (!(blockEntity instanceof CropSticksBlockEntity crop)) {
                continue;
            }
            if (crop.getCrop() == null || !crop.getCrop().canBreed(crop) || CropHelper.isWeed(crop)) {
                continue;
            }
            if (crop.getResistanceStat() >= 28 && random.nextFloat() < 0.5F) {
                continue;
            }
            parents.add(crop);
        }
        return parents;
    }

    private static ICrop selectChildCrop(
            final CropSticksBlockEntity center,
            final List<CropSticksBlockEntity> parents,
            final RandomSource random) {
        if (CropHelper.hasSnowSubsoil(center)
                && parents.stream().anyMatch(parent -> parent.getCrop() == CropRegistry.NETHER_WART)
                && random.nextFloat() < 0.18F) {
            return CropRegistry.TERRA_WART;
        }

        boolean wheatParents = parents.stream().allMatch(parent -> parent.getCrop() == CropRegistry.WHEAT);
        if (wheatParents && random.nextFloat() < 0.12F) {
            return CropRegistry.STICKREED;
        }
        boolean sugarParents = parents.stream().anyMatch(parent -> parent.getCrop() == CropRegistry.SUGARCANE)
                && parents.stream().anyMatch(parent -> parent.getCrop() == CropRegistry.WHEAT);
        if (sugarParents && random.nextFloat() < 0.08F) {
            return CropRegistry.STICKREED;
        }
        boolean reedAndWheat = parents.stream().anyMatch(parent -> parent.getCrop() == CropRegistry.STICKREED)
                && parents.stream().anyMatch(parent -> parent.getCrop() == CropRegistry.WHEAT);
        if (reedAndWheat && random.nextFloat() < 0.06F) {
            return CropRegistry.HOPS;
        }

        // Ore crops — need ore-enriched subsoil or two same-type parent crosses
        boolean ferruParents = parents.stream().allMatch(p -> p.getCrop() == CropRegistry.FERRU);
        if (ferruParents && random.nextFloat() < 0.10F) {
            return CropRegistry.CUPRICUM;
        }
        boolean orexOre = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.FERRU)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.CUPRICUM);
        if (orexOre && random.nextFloat() < 0.06F) {
            return CropRegistry.STANNUM;
        }
        boolean stannumXFerru = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.STANNUM)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.AURELIA);
        if (stannumXFerru && random.nextFloat() < 0.04F) {
            return CropRegistry.CINNABAR;
        }
        boolean ferruParentsAurelia = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.FERRU)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.STANNUM)
                && CropHelper.hasOreSurface(center);
        if (ferruParentsAurelia && random.nextFloat() < 0.05F) {
            return CropRegistry.AURELIA;
        }

        // Food crops — vanilla seed crosses
        boolean wheatXSugar = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.WHEAT)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.SUGARCANE);
        if (wheatXSugar && random.nextFloat() < 0.08F) {
            return CropRegistry.MELON;
        }
        boolean melonXWheat = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.MELON)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.WHEAT);
        if (melonXWheat && random.nextFloat() < 0.07F) {
            return CropRegistry.PUMPKIN;
        }
        boolean hopXWart = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.HOPS)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.NETHER_WART);
        if (hopXWart && random.nextFloat() < 0.06F) {
            return CropRegistry.CACTUS;
        }
        boolean sugarXHops = parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.SUGARCANE)
                && parents.stream().anyMatch(p -> p.getCrop() == CropRegistry.HOPS);
        if (sugarXHops && random.nextFloat() < 0.07F) {
            return CropRegistry.COFFEA;
        }
        boolean coffeaXCoffea = parents.stream().allMatch(p -> p.getCrop() == CropRegistry.COFFEA);
        if (coffeaXCoffea && random.nextFloat() < 0.10F) {
            return CropRegistry.COCOA;
        }

        if (random.nextFloat() < 0.03F) {
            return CropRegistry.WEED;
        }

        CropSticksBlockEntity parent = parents.get(random.nextInt(parents.size()));
        return parent.getCrop();
    }

    private static int blendStat(
            final List<CropSticksBlockEntity> parents,
            final ToIntFunction<CropSticksBlockEntity> getter,
            final RandomSource random) {
        int sum = 0;
        for (CropSticksBlockEntity parent : parents) {
            sum += getter.applyAsInt(parent);
        }
        int average = sum / parents.size();
        int delta = random.nextInt(3) - 1;
        return Mth.clamp(average + delta, 1, 31);
    }
}
