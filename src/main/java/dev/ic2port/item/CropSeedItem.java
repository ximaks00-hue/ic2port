package dev.ic2port.item;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.util.CropSeedHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Seed bag storing crop id and GGR stats (IC2 crop breeding metadata).
 */
public class CropSeedItem extends Item {

    public CropSeedItem(final Properties properties) {
        super(properties);
    }

    public static ICrop resolveCrop(final ItemStack stack) {
        return CropSeedHelper.getCrop(stack);
    }

    @Override
    public Component getName(final ItemStack stack) {
        ICrop crop = CropSeedHelper.getCrop(stack);
        if (crop == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.ic2port.crop_seed.named", crop.getName());
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        ICrop crop = CropSeedHelper.getCrop(stack);
        if (crop == null) {
            tooltip.add(Component.translatable("item.ic2port.crop_seed.empty"));
            return;
        }
        tooltip.add(Component.translatable("item.ic2port.crop_seed.stats",
                CropSeedHelper.getGrowth(stack),
                CropSeedHelper.getGain(stack),
                CropSeedHelper.getResistance(stack)));
        if (CropSeedHelper.getScanLevel(stack) > 0) {
            tooltip.add(Component.translatable("item.ic2port.crop_seed.scan", CropSeedHelper.getScanLevel(stack)));
        }
    }
}
