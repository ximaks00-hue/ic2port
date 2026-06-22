package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.FoamSprayHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * EU-powered CF sprayer with adjustable range and internal foam storage (IC2 electric sprayer).
 */
public class ElectricFoamSprayerItem extends ElectricItem {

    public static final String TAG_FOAM = "FoamStored";
    public static final String TAG_RANGE = "SprayRange";

    public static final double CAPACITY = 2000.0D;
    public static final double ENERGY_PER_BLOCK = 50.0D;
    public static final int FOAM_PER_PELLET = 13;
    public static final int MAX_FOAM = 128;
    public static final int[] RANGE_STEPS = {1, 3, 5, 8};

    public ElectricFoamSprayerItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }

    public static int getFoamStored(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return Math.min(MAX_FOAM, Math.max(0, tag.getInt(TAG_FOAM)));
    }

    public static void setFoamStored(final ItemStack stack, final int amount) {
        stack.getOrCreateTag().putInt(TAG_FOAM, Math.max(0, Math.min(MAX_FOAM, amount)));
    }

    public static int getSprayRange(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_RANGE)) {
            return RANGE_STEPS[1];
        }
        return tag.getInt(TAG_RANGE);
    }

    public static void cycleSprayRange(final ItemStack stack) {
        int current = getSprayRange(stack);
        int nextIndex = 0;
        for (int i = 0; i < RANGE_STEPS.length; i++) {
            if (RANGE_STEPS[i] == current) {
                nextIndex = (i + 1) % RANGE_STEPS.length;
                break;
            }
        }
        stack.getOrCreateTag().putInt(TAG_RANGE, RANGE_STEPS[nextIndex]);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        int placed = FoamSprayHelper.sprayFromFace(
                context.getLevel(),
                player,
                context.getItemInHand(),
                context.getHand(),
                context.getClickedPos().relative(context.getClickedFace()),
                getSprayRange(context.getItemInHand()),
                p -> paySprayCost(context.getItemInHand(), p));
        return placed > 0 ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (otherStack.is(ItemRegistry.FOAM_PELLET.get()) && tryRefillFromPellet(stack, player, otherStack)) {
            player.displayClientMessage(Component.translatable("message.ic2port.electric_foam_sprayer.refilled"), true);
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()) {
            cycleSprayRange(stack);
            player.displayClientMessage(
                    Component.translatable("message.ic2port.electric_foam_sprayer.range", getSprayRange(stack)),
                    true);
            return InteractionResultHolder.success(stack);
        }
        int placed = FoamSprayHelper.sprayFromLook(
                level,
                player,
                stack,
                hand,
                getSprayRange(stack),
                p -> paySprayCost(stack, p));
        return placed > 0 ? InteractionResultHolder.consume(stack) : InteractionResultHolder.pass(stack);
    }

    private boolean paySprayCost(final ItemStack sprayer, final Player player) {
        if (getFoamStored(sprayer) <= 0) {
            player.displayClientMessage(Component.translatable("message.ic2port.electric_foam_sprayer.no_foam"), true);
            return false;
        }
        if (getStoredEnergy(sprayer) < ENERGY_PER_BLOCK) {
            player.displayClientMessage(Component.translatable("message.ic2port.electric_foam_sprayer.no_energy"), true);
            return false;
        }
        if (!player.getAbilities().instabuild) {
            setFoamStored(sprayer, getFoamStored(sprayer) - 1);
            drawEnergy(sprayer, ENERGY_PER_BLOCK);
        }
        return true;
    }

    public static boolean tryRefillFromPellet(final ItemStack sprayer, final Player player, final ItemStack pellet) {
        if (!pellet.is(ItemRegistry.FOAM_PELLET.get())) {
            return false;
        }
        int space = MAX_FOAM - getFoamStored(sprayer);
        if (space < FOAM_PER_PELLET) {
            return false;
        }
        setFoamStored(sprayer, getFoamStored(sprayer) + FOAM_PER_PELLET);
        if (!player.getAbilities().instabuild) {
            pellet.shrink(1);
        }
        return true;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ic2port.electric_foam_sprayer.foam", getFoamStored(stack), MAX_FOAM)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ic2port.electric_foam_sprayer.range", getSprayRange(stack))
                .withStyle(ChatFormatting.GRAY));
        int stored = (int) Math.round(getStoredEnergy(stack));
        int max = (int) Math.round(getMaxEnergy());
        tooltip.add(Component.translatable("item.ic2port.electric_foam_sprayer.energy", stored, max)
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.ic2port.electric_foam_sprayer.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
