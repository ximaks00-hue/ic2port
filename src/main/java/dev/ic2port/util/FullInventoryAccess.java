package dev.ic2port.util;

import net.minecraftforge.items.IItemHandler;

/**
 * Block entities that expose a restricted automation handler but retain a full inventory for wrench spill.
 */
public interface FullInventoryAccess {

    IItemHandler getFullItemHandler();
}
