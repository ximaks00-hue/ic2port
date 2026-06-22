package dev.ic2port.block;

import dev.ic2port.blockentity.TeleporterBlockEntity;
import dev.ic2port.item.FrequencyTransmitterItem;
import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TeleporterBlock extends BaseEntityBlock {

    public TeleporterBlock(final Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new TeleporterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state,
                                                                   final BlockEntityType<T> type) {
        return null;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level level, final BlockPos pos,
                                  final Player player, final InteractionHand hand, final BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof FrequencyTransmitterItem && FrequencyTransmitterItem.hasLinkedPos(held)) {
            BlockPos dest = FrequencyTransmitterItem.getLinkedPos(held);
            if (dest.equals(pos)) {
                player.displayClientMessage(Component.translatable("block.ic2port.teleporter.same_block"), true);
                return InteractionResult.CONSUME;
            }
            boolean success = teleporter.teleportPlayer(player, dest);
            if (!success) {
                player.displayClientMessage(Component.translatable("block.ic2port.teleporter.no_energy"), true);
            }
            return InteractionResult.CONSUME;
        }

        double eu = teleporter.getStoredEnergy();
        player.displayClientMessage(
                Component.translatable("block.ic2port.teleporter.status",
                        (int) eu, (int) TeleporterBlockEntity.ENERGY_CAPACITY),
                true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }
}
