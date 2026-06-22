package dev.ic2port.block;

import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * Blast-resistant IC2-style reinforced door.
 */
public class ReinforcedDoorBlock extends DoorBlock {

    public ReinforcedDoorBlock(final Properties properties) {
        super(properties, BlockSetType.IRON);
    }
}
