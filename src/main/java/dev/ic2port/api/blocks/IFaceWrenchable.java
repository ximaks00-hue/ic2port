package dev.ic2port.api.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks that support per-face wrench actions (pipe disconnect, cover removal).
 */
public interface IFaceWrenchable extends IWrenchable {

    /**
     * @return {@code true} if the wrench action was handled (no dismantle)
     */
    boolean onWrenchFace(final UseOnContext context, final BlockState state, final Direction face);
}
