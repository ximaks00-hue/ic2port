package dev.ic2port.item;

/**
 * Pulls filtered items from adjacent inventories into a machine buffer.
 */
public class ImportUpgradeItem extends TransportUpgradeItem {

    public ImportUpgradeItem(final Properties properties) {
        super(properties, "item.ic2port.import_upgrade.tooltip");
    }
}
