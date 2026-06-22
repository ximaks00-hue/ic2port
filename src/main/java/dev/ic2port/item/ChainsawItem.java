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
 * LV electric chainsaw — cuts logs and attacks mobs efficiently.
 * Uses 200 EU per attack and 100 EU per log mined.
 */
public class ChainsawItem extends ElectricItem {

    public static final double CAPACITY = 10_000.0D;
    public static final double EU_PER_BLOCK = 100.0D;
    public static final double EU_PER_ATTACK = 200.0D;
    public static final float ATTACK_DAMAGE_BONUS = 5.0F;
    private static final float MINING_SPEED = 8.0F;

    public ChainsawItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }

    @Override
    public float getDestroySpeed(final ItemStack stack, final BlockState state) {
        if (getStoredEnergy(stack) < EU_PER_BLOCK) {
            return 1.0F;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS)) {
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
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        if (getStoredEnergy(stack) >= EU_PER_ATTACK) {
            drawEnergy(stack, EU_PER_ATTACK);
        }
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(final ItemStack stack, final BlockState state) {
        return getStoredEnergy(stack) >= EU_PER_BLOCK
                && (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LOGS));
    }

    @Override
    public boolean canPerformAction(final ItemStack stack, final ToolAction toolAction) {
        return toolAction == ToolActions.AXE_DIG
                || toolAction == ToolActions.AXE_STRIP
                || toolAction == ToolActions.AXE_SCRAPE
                || toolAction == ToolActions.AXE_WAX_OFF;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.chainsaw.info",
                (int) getStoredEnergy(stack), (int) CAPACITY));
    }
}
