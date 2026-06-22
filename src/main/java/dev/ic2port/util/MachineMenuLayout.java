package dev.ic2port.util;

/**
 * Shared layout constants for machine GUIs with upgrade slots.
 */
public final class MachineMenuLayout {

    public static final int UPGRADE_SLOT_COUNT = 4;
    public static final int MACHINE_SLOT_COUNT = 2;

    public static final int IMAGE_WIDTH = 202;
    public static final int IMAGE_HEIGHT = 166;

    public static final int UPGRADE_PANEL_X = 176;
    public static final int UPGRADE_PANEL_Y = 8;
    public static final int UPGRADE_PANEL_WIDTH = 22;
    public static final int UPGRADE_PANEL_HEIGHT = 82;

    public static final int UPGRADE_SLOT_X = 179;
    public static final int UPGRADE_SLOT_START_Y = 17;
    public static final int UPGRADE_SLOT_SPACING = 18;

    public static final int PLAYER_INVENTORY_START_Y = 84;
    public static final int HOTBAR_Y = 142;

    public static final int SLOT_INPUT_X = 56;
    public static final int SLOT_INPUT_Y = 35;
    public static final int SLOT_OUTPUT_X = 116;
    public static final int SLOT_OUTPUT_Y = 35;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_MAX_PROGRESS = 1;
    public static final int DATA_STORED_ENERGY = 2;
    public static final int DATA_MAX_ENERGY = 3;

    private MachineMenuLayout() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static int upgradeSlotIndex(final int upgradeOffset) {
        return MACHINE_SLOT_COUNT + upgradeOffset;
    }

    public static int playerInventoryStartIndex() {
        return MACHINE_SLOT_COUNT + UPGRADE_SLOT_COUNT;
    }
}
