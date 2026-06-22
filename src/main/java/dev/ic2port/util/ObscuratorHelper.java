package dev.ic2port.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * NBT storage for {@link dev.ic2port.item.ObscuratorItem} texture samples.
 */
public final class ObscuratorHelper {

    private static final String TAG_DISGUISE = "DisguiseState";

    private ObscuratorHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean storeSample(final ItemStack stack, final BlockState state) {
        if (state.isAir()) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_DISGUISE, NbtUtils.writeBlockState(state));
        return true;
    }

    @Nullable
    public static BlockState getSample(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_DISGUISE)) {
            return null;
        }
        return NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(TAG_DISGUISE));
    }

    public static boolean hasSample(final ItemStack stack) {
        return getSample(stack) != null;
    }

    public static void clearSample(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_DISGUISE);
        }
    }
}
