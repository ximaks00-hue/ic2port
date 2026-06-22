package dev.ic2port.item;

import dev.ic2port.util.TeleportLink;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Links two teleporters. Right-click a teleporter block to store its coordinates and dimension.
 * Use the linked transmitter on another teleporter to teleport there.
 */
public class FrequencyTransmitterItem extends Item {

    private static final String TAG_POS = "LinkedPos";
    private static final String TAG_DIM = "LinkedDim";

    public FrequencyTransmitterItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!(level.getBlockEntity(pos) instanceof dev.ic2port.blockentity.TeleporterBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        var player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() || !hasLinkedTarget(stack)) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_POS, pos.asLong());
            tag.putString(TAG_DIM, level.dimension().location().toString());
            player.displayClientMessage(
                    Component.translatable("item.ic2port.frequency_transmitter.linked",
                            pos.getX(), pos.getY(), pos.getZ()), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level level,
                                 final List<Component> tooltip, final TooltipFlag flag) {
        if (hasLinkedTarget(stack)) {
            TeleportLink link = getLinkedTarget(stack);
            tooltip.add(Component.translatable("item.ic2port.frequency_transmitter.target",
                    link.pos().getX(), link.pos().getY(), link.pos().getZ()));
            tooltip.add(Component.translatable("item.ic2port.frequency_transmitter.dimension",
                    link.dimension().location()));
        } else {
            tooltip.add(Component.translatable("item.ic2port.frequency_transmitter.unlinked"));
        }
    }

    public static boolean hasLinkedTarget(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_POS);
    }

    /** @deprecated use {@link #hasLinkedTarget(ItemStack)} */
    @Deprecated
    public static boolean hasLinkedPos(final ItemStack stack) {
        return hasLinkedTarget(stack);
    }

    public static TeleportLink getLinkedTarget(final ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        BlockPos pos = BlockPos.of(tag.getLong(TAG_POS));
        ResourceKey<Level> dimension = resolveDimension(tag);
        return new TeleportLink(dimension, pos);
    }

    /** @deprecated use {@link #getLinkedTarget(ItemStack)} */
    @Deprecated
    public static BlockPos getLinkedPos(final ItemStack stack) {
        return getLinkedTarget(stack).pos();
    }

    private static ResourceKey<Level> resolveDimension(final CompoundTag tag) {
        if (tag.contains(TAG_DIM)) {
            ResourceLocation id = new ResourceLocation(tag.getString(TAG_DIM));
            return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id);
        }
        return Level.OVERWORLD;
    }
}
