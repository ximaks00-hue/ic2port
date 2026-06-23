package dev.ic2port.tube.handler;

import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TubeRole;
import dev.ic2port.tube.TubeRoleHandler;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Destroys items entering this tube segment.
 */
public final class VoidTubeHandler implements TubeRoleHandler {

    public static final VoidTubeHandler INSTANCE = new VoidTubeHandler();

    private VoidTubeHandler() {
    }

    @Override
    public TubeRole role() {
        return TubeRole.VOID;
    }

    @Override
    public boolean acceptFromNetwork(final TubeBlockEntity tube, final BlockState state) {
        return true;
    }
}
