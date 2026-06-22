package dev.ic2port.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Links two teleporters. Right-click a teleporter block to store its coordinates.
 * Shift+right-click the other teleporter to link them.
 */
public class FrequencyTransmitterItem extends Item {

    private static final String TAG_POS = "LinkedPos";

    public FrequencyTransmitterItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof dev.ic2port.blockentity.TeleporterBlockEntity) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_POS, pos.asLong());
            context.getPlayer().displayClientMessage(
                    Component.translatable("item.ic2port.frequency_transmitter.linked",
                            pos.getX(), pos.getY(), pos.getZ()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level level,
                                 final List<Component> tooltip, final TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_POS)) {
            BlockPos pos = BlockPos.of(tag.getLong(TAG_POS));
            tooltip.add(Component.translatable("item.ic2port.frequency_transmitter.target",
                    pos.getX(), pos.getY(), pos.getZ()));
        } else {
            tooltip.add(Component.translatable("item.ic2port.frequency_transmitter.unlinked"));
        }
    }

    public static boolean hasLinkedPos(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_POS);
    }

    public static BlockPos getLinkedPos(final ItemStack stack) {
        return BlockPos.of(stack.getOrCreateTag().getLong(TAG_POS));
    }
}
