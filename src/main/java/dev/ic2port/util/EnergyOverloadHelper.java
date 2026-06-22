package dev.ic2port.util;

import dev.ic2port.setup.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

/**
 * Overvoltage explosion for non-{@link dev.ic2port.blockentity.BaseMachineBlockEntity} EU acceptors.
 */
public final class EnergyOverloadHelper {

    private EnergyOverloadHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Spills contents, removes the block, and creates an explosion when {@code incomingTier} exceeds
     * {@code machineTier}.
     *
     * @return {@code true} if an explosion was triggered
     */
    public static boolean tryExplode(
            final Level level,
            final BlockPos pos,
            final @Nullable BlockEntity blockEntity,
            final int incomingTier,
            final int machineTier) {
        if (level == null || level.isClientSide || blockEntity == null || incomingTier <= machineTier) {
            return false;
        }

        spillContents(level, pos, blockEntity);

        final double centerX = pos.getX() + 0.5D;
        final double centerY = pos.getY() + 0.5D;
        final double centerZ = pos.getZ() + 0.5D;
        level.removeBlock(pos, false);

        final float radius = ModConfig.EXPLOSION_BASE_RADIUS.get().floatValue()
                + (incomingTier - machineTier) * 1.5F;
        level.explode(null, centerX, centerY, centerZ, radius, Level.ExplosionInteraction.BLOCK);
        return true;
    }

    private static void spillContents(final Level level, final BlockPos pos, final BlockEntity blockEntity) {
        if (blockEntity instanceof FullInventoryAccess access) {
            BlockEntitySpillHelper.spillItems(level, pos, access.getFullItemHandler());
        } else {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler ->
                    BlockEntitySpillHelper.spillItems(level, pos, handler));
        }
        BlockEntitySpillHelper.spillFluids(level, pos, blockEntity);
    }
}
