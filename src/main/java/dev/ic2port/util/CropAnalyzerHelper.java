package dev.ic2port.util;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.api.crops.ICropTile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Formats crop scan results for the {@link dev.ic2port.item.CropnalyzerItem}.
 */
public final class CropAnalyzerHelper {

    private CropAnalyzerHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void displayScan(final ICropTile tile, final Player player) {
        ICrop crop = tile.getCrop();
        if (crop == null) {
            player.displayClientMessage(Component.translatable("message.ic2port.cropnalyzer.no_crop"), true);
            return;
        }

        int scan = tile.getScanLevel();
        if (scan <= 0) {
            player.displayClientMessage(Component.translatable("message.ic2port.cropnalyzer.unknown"), true);
            return;
        }

        player.displayClientMessage(
                Component.translatable("message.ic2port.cropnalyzer.crop", crop.getName()),
                true);

        if (scan >= 2) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.cropnalyzer.tier",
                            crop.getProperties().tier()),
                    true);
        }
        if (scan >= 3) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.cropnalyzer.stats",
                            tile.getGrowthStat(),
                            tile.getGainStat(),
                            tile.getResistanceStat()),
                    true);
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.cropnalyzer.stage",
                            tile.getGrowthStage(),
                            crop.getGrowthSteps()),
                    true);
        }
        if (scan >= 4 && crop.getAttributes().length > 0) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.ic2port.cropnalyzer.attributes",
                            String.join(", ", crop.getAttributes())),
                    true);
        }
    }
}
