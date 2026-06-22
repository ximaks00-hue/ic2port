package dev.ic2port.util;

import dev.ic2port.Reference;
import dev.ic2port.blockentity.BaseMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Dismantles ic2port blocks — spills inventories and optionally drops the block (IC2 wrench semantics).
 */
public final class WrenchHelper {

    /** Drop chance when not sneaking (machine may be damaged). */
    public static final double STANDARD_DROP_CHANCE = 0.8D;
    /** Drop chance when sneaking for lossless dismantling. */
    public static final double SNEAK_DROP_CHANCE = 0.95D;

    private WrenchHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isIc2PortBlock(final Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id != null && Reference.MOD_ID.equals(id.getNamespace());
    }

    public static boolean tryDismantle(final UseOnContext context) {
        return tryDismantle(context, STANDARD_DROP_CHANCE, SNEAK_DROP_CHANCE);
    }

    /**
     * @return {@code true} if the block was an ic2port block and dismantling was attempted
     */
    public static boolean tryDismantle(
            final UseOnContext context,
            final double standardDropChance,
            final double sneakDropChance) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null || !isIc2PortBlock(state.getBlock())) {
            return false;
        }
        if (player.blockActionRestricted(level, pos, GameType.SURVIVAL)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        spillInventories(level, pos, blockEntity);

        double dropChance = player.isShiftKeyDown() ? sneakDropChance : standardDropChance;
        boolean dropBlock = level.random.nextDouble() < dropChance;
        if (dropBlock && level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = Block.getDrops(
                    state,
                    serverLevel,
                    pos,
                    blockEntity,
                    player,
                    context.getItemInHand());
            for (ItemStack drop : drops) {
                Block.popResource(level, pos, drop);
            }
        } else if (!dropBlock) {
            player.displayClientMessage(
                    Component.translatable("message.ic2port.wrench.damaged"),
                    true);
        }

        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.6F, 1.4F);
        return true;
    }

    private static void spillInventories(
            final Level level,
            final BlockPos pos,
            final @Nullable BlockEntity blockEntity) {
        if (blockEntity == null) {
            return;
        }
        if (blockEntity instanceof BaseMachineBlockEntity machine) {
            BlockEntitySpillHelper.spillItems(level, pos, machine.getFullItemHandler());
        } else {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler ->
                    BlockEntitySpillHelper.spillItems(level, pos, handler));
        }
        BlockEntitySpillHelper.spillFluids(level, pos, blockEntity);
    }
}
