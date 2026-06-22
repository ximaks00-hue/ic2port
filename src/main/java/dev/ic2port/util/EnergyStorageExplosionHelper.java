package dev.ic2port.util;

import dev.ic2port.setup.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Shared overload explosion for EU storage blocks with optional item slots.
 */
public final class EnergyStorageExplosionHelper {

    private EnergyStorageExplosionHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void explode(
            final Level level,
            final BlockPos pos,
            final IItemHandler itemHandler,
            final int machineTier,
            final int incomingTier) {
        final double centerX = pos.getX() + 0.5D;
        final double centerY = pos.getY() + 0.5D;
        final double centerZ = pos.getZ() + 0.5D;

        if (itemHandler != null) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                final ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, centerX, centerY, centerZ, stack.copy());
                    if (itemHandler instanceof IItemHandlerModifiable modifiable) {
                        modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        }

        final float radius = ModConfig.EXPLOSION_BASE_RADIUS.get().floatValue()
                + (incomingTier - machineTier) * 1.5F;
        level.explode(null, centerX, centerY, centerZ, radius, Level.ExplosionInteraction.BLOCK);
        level.removeBlock(pos, false);
    }
}
