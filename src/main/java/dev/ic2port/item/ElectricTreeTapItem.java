package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.util.RubberResinExtractor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * LV electric tree tap — draws EU instead of durability and overtaps less often.
 */
public class ElectricTreeTapItem extends ElectricItem {

    public static final double CAPACITY = 1000.0D;
    public static final double ENERGY_PER_TAP = 50.0D;

    public ElectricTreeTapItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RubberWoodBlock)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player != null && player.blockActionRestricted(level, pos, GameType.SURVIVAL)) {
            return InteractionResult.FAIL;
        }
        return RubberResinExtractor.tryExtract(
                state,
                level,
                pos,
                player,
                context.getHand(),
                context.getItemInHand(),
                this::payTapCost,
                RubberResinExtractor.ELECTRIC_OVERTAP_CHANCE);
    }

    public boolean payTapCostForBlock(final ItemStack tool, final Player player, final InteractionHand hand) {
        return payTapCost(tool, player, hand);
    }

    private boolean payTapCost(final ItemStack tool, final Player player, final InteractionHand hand) {
        if (getStoredEnergy(tool) < ENERGY_PER_TAP) {
            player.displayClientMessage(Component.translatable("message.ic2port.electric_tree_tap.no_energy"), true);
            return false;
        }
        drawEnergy(tool, ENERGY_PER_TAP);
        syncHolderInventory(player);
        return true;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int stored = (int) Math.round(getStoredEnergy(stack));
        int max = (int) Math.round(getMaxEnergy());
        tooltip.add(Component.translatable("item.ic2port.electric_tree_tap.energy", stored, max)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                        "item.ic2port.electric_tree_tap.cost",
                        (int) ENERGY_PER_TAP)
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
