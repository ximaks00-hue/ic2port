package dev.ic2port.item;

import net.minecraft.world.item.Item;

/**
 * Blueprint item for the Terraformer machine.
 * The specific mode is identified by which blueprint item is loaded.
 */
public class TerraformerBlueprintItem extends Item {

    public enum Mode {
        CULTIVATION,
        IRRIGATION,
        DESERTIFICATION
    }

    private final Mode mode;

    public TerraformerBlueprintItem(final Mode mode, final Properties properties) {
        super(properties);
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }
}
