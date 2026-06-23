package dev.ic2port.util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Filter and facing configuration stored on transport upgrade items.
 */
public final class TransportUpgradeHelper {

    public static final String TAG_FILTER = "TransportFilter";
    public static final String TAG_SIDE = "TransportSide";

    private TransportUpgradeHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ItemStack getFilter(final ItemStack upgrade) {
        CompoundTag tag = upgrade.getTag();
        if (tag == null || !tag.contains(TAG_FILTER)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound(TAG_FILTER));
    }

    public static void setFilter(final ItemStack upgrade, final ItemStack filter) {
        CompoundTag tag = upgrade.getOrCreateTag();
        if (filter.isEmpty()) {
            tag.remove(TAG_FILTER);
            if (tag.isEmpty()) {
                upgrade.setTag(null);
            }
            return;
        }
        tag.put(TAG_FILTER, filter.copyWithCount(1).save(new CompoundTag()));
    }

    /**
     * @return configured side, or {@code null} when all sides are active
     */
    @Nullable
    public static Direction getSide(final ItemStack upgrade) {
        CompoundTag tag = upgrade.getTag();
        if (tag == null || !tag.contains(TAG_SIDE)) {
            return null;
        }
        int ordinal = tag.getInt(TAG_SIDE);
        if (ordinal < 0) {
            return null;
        }
        if (ordinal >= Direction.values().length) {
            return null;
        }
        return Direction.from3DDataValue(ordinal);
    }

    public static void cycleSide(final ItemStack upgrade) {
        CompoundTag tag = upgrade.getOrCreateTag();
        int current = tag.contains(TAG_SIDE) ? tag.getInt(TAG_SIDE) : -1;
        int next = current + 1;
        if (next >= Direction.values().length) {
            tag.putInt(TAG_SIDE, -1);
        } else {
            tag.putInt(TAG_SIDE, next);
        }
    }

    public static Component sideTooltip(final ItemStack upgrade) {
        Direction side = getSide(upgrade);
        if (side == null) {
            return Component.translatable("item.ic2port.transport_upgrade.side.all");
        }
        return Component.translatable("item.ic2port.transport_upgrade.side", side.getName());
    }
}
