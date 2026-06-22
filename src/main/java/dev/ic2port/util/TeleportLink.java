package dev.ic2port.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Stored teleporter destination — position and dimension.
 */
public record TeleportLink(ResourceKey<Level> dimension, BlockPos pos) {
}
