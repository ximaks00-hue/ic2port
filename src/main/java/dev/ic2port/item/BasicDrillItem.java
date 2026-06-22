package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tier-1 electric mining drill (LV). Cheaper to craft than the Advanced Drill,
 * no mode switching — uses 50 EU/block.
 */
public class BasicDrillItem extends ElectricItem {

    public static final double CAPACITY = 10_000.0D;
    public static final double EU_PER_BLOCK = 50.0D;
    private static final float MINING_SPEED = 8.0F;

    public BasicDrillItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }

    @Override
    public float getDestroySpeed(final ItemStack stack, final BlockState state) {
        if (getStoredEnergy(stack) < EU_PER_BLOCK) {
            return 1.0F;
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return MINING_SPEED;
        }
        return 1.0F;
    }

    @Override
    public boolean mineBlock(final ItemStack stack, final Level level, final BlockState state,
                             final BlockPos pos, final LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) > 0.0F) {
            drawEnergy(stack, EU_PER_BLOCK);
        }
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(final ItemStack stack, final BlockState state) {
        return getStoredEnergy(stack) >= EU_PER_BLOCK
                && (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL));
    }

    @Override
    public boolean canPerformAction(final ItemStack stack, final ToolAction toolAction) {
        return toolAction == ToolActions.PICKAXE_DIG || toolAction == ToolActions.SHOVEL_DIG;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.basic_drill.info",
                (int) getStoredEnergy(stack), (int) CAPACITY, (int) EU_PER_BLOCK));
    }
}
