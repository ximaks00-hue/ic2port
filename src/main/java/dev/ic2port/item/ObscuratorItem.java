package dev.ic2port.item;

import dev.ic2port.block.ConstructionFoamBlock;
import dev.ic2port.blockentity.ConstructionFoamBlockEntity;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.util.ObscuratorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Copies block textures onto construction foam (IC2 obscurator).
 */
public class ObscuratorItem extends Item {

    public ObscuratorItem(final Properties properties) {
        super(properties.durability(32));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clicked = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (ObscuratorHelper.storeSample(stack, clicked)) {
                player.displayClientMessage(Component.translatable("message.ic2port.obscurator.copied", clicked.getBlock().getName()), true);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        if (!clicked.is(BlockRegistry.CONSTRUCTION_FOAM.get())) {
            return InteractionResult.PASS;
        }
        if (!ObscuratorHelper.hasSample(stack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ConstructionFoamBlockEntity foam)) {
            return InteractionResult.PASS;
        }
        BlockState disguise = ObscuratorHelper.getSample(stack);
        if (disguise == null) {
            return InteractionResult.PASS;
        }
        foam.setDisguise(disguise);
        level.setBlock(pos, clicked.setValue(ConstructionFoamBlock.CAMOUFLAGED, true), 3);
        if (player != null && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        ObscuratorHelper.clearSample(stack);
        player.displayClientMessage(Component.translatable("message.ic2port.obscurator.cleared"), true);
        return InteractionResultHolder.success(stack);
    }
}
