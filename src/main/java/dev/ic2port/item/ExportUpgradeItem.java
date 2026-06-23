package dev.ic2port.item;

/**
 * Pushes filtered items from a machine buffer into adjacent inventories.
 */
public class ExportUpgradeItem extends TransportUpgradeItem {

    public ExportUpgradeItem(final Properties properties) {
        super(properties, "item.ic2port.export_upgrade.tooltip");
    }
}
