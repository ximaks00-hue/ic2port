package dev.ic2port.item;

import net.minecraft.world.item.Item;

/**
 * Trade-O-Mat currency with a fixed copper value.
 */
public class TradeCoinItem extends Item {

    public enum CoinTier {
        COPPER(1),
        SILVER(100),
        GOLD(10_000);

        private final int value;

        CoinTier(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final CoinTier tier;

    public TradeCoinItem(final Properties properties, final CoinTier tier) {
        super(properties);
        this.tier = tier;
    }

    public CoinTier getTier() {
        return tier;
    }

    public int getCoinValue() {
        return tier.getValue();
    }
}
