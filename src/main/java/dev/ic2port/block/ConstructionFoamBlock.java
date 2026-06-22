package dev.ic2port.block;

import dev.ic2port.blockentity.ConstructionFoamBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Dried IC2-style construction foam — blast-resistant, paintable, and obscurator-compatible.
 */
public class ConstructionFoamBlock extends BaseEntityBlock {

    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    public static final BooleanProperty CAMOUFLAGED = BooleanProperty.create("camouflaged");

    public ConstructionFoamBlock(final Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(COLOR, DyeColor.WHITE)
                .setValue(CAMOUFLAGED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR, CAMOUFLAGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ConstructionFoamBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return state.getValue(CAMOUFLAGED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }
}
