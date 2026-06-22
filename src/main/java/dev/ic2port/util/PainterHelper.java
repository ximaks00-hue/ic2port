package dev.ic2port.util;

import dev.ic2port.block.ConstructionFoamBlock;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Recolors construction foam and common vanilla dyeable blocks (IC2 painter).
 */
public final class PainterHelper {

    private static final Map<DyeColor, Block> WOOL = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, Block> CARPET = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, Block> CONCRETE = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, Block> CONCRETE_POWDER = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, Block> TERRACOTTA = new EnumMap<>(DyeColor.class);

    static {
        register(WOOL, "wool");
        register(CARPET, "carpet");
        register(CONCRETE, "concrete");
        register(CONCRETE_POWDER, "concrete_powder");
        register(TERRACOTTA, "terracotta");
    }

    private PainterHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static void register(final Map<DyeColor, Block> map, final String suffix) {
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation id = new ResourceLocation("minecraft", color.getName() + "_" + suffix);
            Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR);
            if (block != Blocks.AIR) {
                map.put(color, block);
            }
        }
    }

    public static boolean canPaint(final BlockState state) {
        Block block = state.getBlock();
        if (block == BlockRegistry.CONSTRUCTION_FOAM.get()) {
            return true;
        }
        return WOOL.containsValue(block)
                || CARPET.containsValue(block)
                || CONCRETE.containsValue(block)
                || CONCRETE_POWDER.containsValue(block)
                || TERRACOTTA.containsValue(block);
    }

    public static boolean paintBlock(final Level level, final BlockPos pos, final DyeColor color) {
        BlockState state = level.getBlockState(pos);
        if (!canPaint(state)) {
            return false;
        }
        BlockState painted = recolor(state, color);
        if (painted == null || painted == state) {
            return false;
        }
        level.setBlock(pos, painted, Block.UPDATE_ALL);
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState recolor(final BlockState state, final DyeColor color) {
        Block block = state.getBlock();
        if (block == BlockRegistry.CONSTRUCTION_FOAM.get()) {
            return state.setValue(ConstructionFoamBlock.COLOR, color);
        }
        Block target = resolveTarget(block, color);
        if (target == null || target == block) {
            return null;
        }
        BlockState targetDefault = target.defaultBlockState();
        BlockState result = targetDefault;
        for (Property property : state.getProperties()) {
            if (targetDefault.hasProperty(property) && property != BedBlock.PART && property != BedBlock.OCCUPIED) {
                Object value = state.getValue(property);
                if (property.getPossibleValues().contains(value)) {
                    result = copyProperty(result, property, value);
                }
            }
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState copyProperty(final BlockState state, final Property property, final Object value) {
        return state.setValue(property, (Comparable) value);
    }

    @Nullable
    private static Block resolveTarget(final Block block, final DyeColor color) {
        if (WOOL.containsValue(block)) {
            return WOOL.get(color);
        }
        if (CARPET.containsValue(block)) {
            return CARPET.get(color);
        }
        if (CONCRETE.containsValue(block)) {
            return CONCRETE.get(color);
        }
        if (CONCRETE_POWDER.containsValue(block)) {
            return CONCRETE_POWDER.get(color);
        }
        if (TERRACOTTA.containsValue(block)) {
            return TERRACOTTA.get(color);
        }
        return null;
    }
}
