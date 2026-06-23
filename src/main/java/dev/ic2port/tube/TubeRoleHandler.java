package dev.ic2port.tube;

import dev.ic2port.blockentity.TubeBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Role-specific tick and accept hooks for tube segments.
 */
public interface TubeRoleHandler {

    TubeRole role();

    default boolean supportsExtraction() {
        return false;
    }

    default boolean tickServer(final TubeBlockEntity tube, final BlockState state) {
        return false;
    }

    default boolean acceptFromNetwork(final TubeBlockEntity tube, final BlockState state) {
        return false;
    }
}
