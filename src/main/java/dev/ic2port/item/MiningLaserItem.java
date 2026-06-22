package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;

/**
 * HV mining laser — breaks the targeted block up to 32 blocks away (2000 EU/block).
 */
public class MiningLaserItem extends ElectricItem {

    public static final double CAPACITY = 300_000.0D;
    public static final double EU_PER_BLOCK = 2000.0D;
    public static final int MAX_RANGE = 32;

    public MiningLaserItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.HV);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (getStoredEnergy(stack) < EU_PER_BLOCK) {
            player.displayClientMessage(Component.translatable("message.ic2port.mining_laser.no_energy"), true);
            return InteractionResultHolder.fail(stack);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(MAX_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.fail(stack);

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) return InteractionResultHolder.fail(stack);
        if (!state.canSurvive(level, pos) || !state.getBlock().canHarvestBlock(state, level, pos, player)) {
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlock(pos, true, player);
        }
        drawEnergy(stack, EU_PER_BLOCK);
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean canPerformAction(final ItemStack stack, final net.minecraftforge.common.ToolAction toolAction) {
        if (getStoredEnergy(stack) < EU_PER_BLOCK) {
            return false;
        }
        return toolAction == ToolActions.PICKAXE_DIG;
    }
}
