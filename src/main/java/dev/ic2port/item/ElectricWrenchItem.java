package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.util.WrenchHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MV electric wrench — dismantles IC2 blocks. Costs 500 EU per use.
 * When the player is sneaking, it has a higher chance to drop the block without damage.
 */
public class ElectricWrenchItem extends ElectricItem {

    public static final double CAPACITY = 40_000.0D;
    public static final double EU_PER_USE = 500.0D;

    public ElectricWrenchItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.MV);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (level.isClientSide || player == null) {
            return InteractionResult.SUCCESS;
        }
        if (getStoredEnergy(stack) < EU_PER_USE) {
            player.displayClientMessage(
                    Component.translatable("message.ic2port.electric_wrench.no_energy"),
                    true);
            return InteractionResult.FAIL;
        }

        var blockState = level.getBlockState(context.getClickedPos());
        if (blockState.is(Blocks.AIR) || !WrenchHelper.isIc2PortBlock(blockState.getBlock())) {
            return InteractionResult.PASS;
        }

        if (!WrenchHelper.tryDismantle(context)) {
            return InteractionResult.FAIL;
        }

        drawEnergy(stack, EU_PER_USE);
        player.getInventory().setChanged();
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.ic2port.electric_wrench.info",
                (int) getStoredEnergy(stack), (int) CAPACITY, (int) EU_PER_USE));
    }
}
