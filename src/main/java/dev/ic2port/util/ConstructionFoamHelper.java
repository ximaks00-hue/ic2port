package dev.ic2port.util;

import dev.ic2port.block.ConstructionFoamBlock;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Drying wet construction foam into colored hardened foam.
 */
public final class ConstructionFoamHelper {

    private ConstructionFoamHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isDryingAgent(final ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.SAND)) {
            return true;
        }
        return Block.byItem(stack.getItem()) instanceof ConcretePowderBlock;
    }

    @Nullable
    public static DyeColor getDyeFromAgent(final ItemStack stack) {
        if (stack.is(Items.SAND)) {
            return DyeColor.WHITE;
        }
        Block block = Block.byItem(stack.getItem());
        if (block instanceof ConcretePowderBlock) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id.getPath().endsWith("_concrete_powder")) {
                String colorName = id.getPath().substring(0, id.getPath().length() - "_concrete_powder".length());
                return DyeColor.byName(colorName, DyeColor.WHITE);
            }
        }
        return null;
    }

    public static boolean dryBlock(final Level level, final BlockPos pos, final @Nullable DyeColor color) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockRegistry.WET_CONSTRUCTION_FOAM.get())) {
            return false;
        }
        DyeColor foamColor = color != null ? color : DyeColor.WHITE;
        BlockState dried = BlockRegistry.CONSTRUCTION_FOAM.get()
                .defaultBlockState()
                .setValue(ConstructionFoamBlock.COLOR, foamColor);
        level.setBlock(pos, dried, Block.UPDATE_ALL);
        return true;
    }
}
