package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.util.CropAnalyzerHelper;
import dev.ic2port.util.StationaryCropAnalyzerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handheld EU-powered crop scanner. Costs {@value #EU_PER_SCAN} EU per scan level advance,
 * matching the stationary {@link dev.ic2port.blockentity.CropAnalyzerBlockEntity}.
 */
public class CropnalyzerItem extends ElectricItem {

    public static final double CAPACITY = 50_000.0D;
    public static final double EU_PER_SCAN = StationaryCropAnalyzerHelper.ENERGY_PER_SCAN_LEVEL;
    private static final int USE_COOLDOWN_TICKS = 10;

    public CropnalyzerItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.MV);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof CropSticksBlockEntity crop)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (getStoredEnergy(stack) < EU_PER_SCAN) {
            player.displayClientMessage(
                    Component.translatable("message.ic2port.cropnalyzer.no_energy").withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.FAIL;
        }

        if (!crop.tryScan(player)) {
            player.displayClientMessage(
                    Component.translatable("message.ic2port.cropnalyzer.no_crop"),
                    true);
            return InteractionResult.FAIL;
        }

        drawEnergy(stack, EU_PER_SCAN);
        player.getInventory().setChanged();
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        level.playSound(
                null,
                context.getClickedPos(),
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundSource.PLAYERS,
                0.5F,
                1.2F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int stored = (int) Math.round(getStoredEnergy(stack));
        tooltip.add(Component.translatable("item.ic2port.cropnalyzer.energy", stored, (int) CAPACITY)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.cropnalyzer.cost", (int) EU_PER_SCAN)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
