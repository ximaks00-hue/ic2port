package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Universal electric mining tool — pickaxe and shovel in one, with switchable power modes.
 */
public class DiamondDrillItem extends ElectricItem {

    public static final double CAPACITY = 30000.0D;
    public static final String DRILL_MODE_TAG = "DrillMode";

    private static final float HAND_SPEED = 1.0F;

    public DiamondDrillItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.LV);
    }

    public enum DrillMode {
        NORMAL(100, 12.0F, "item.ic2port.diamond_drill.mode.normal", ChatFormatting.GREEN),
        LOW_POWER(20, 6.0F, "item.ic2port.diamond_drill.mode.low_power", ChatFormatting.YELLOW),
        SILK_TOUCH(400, 12.0F, "item.ic2port.diamond_drill.mode.silk_touch", ChatFormatting.AQUA);

        private final int energyCost;
        private final float miningSpeed;
        private final String translationKey;
        private final ChatFormatting chatColor;

        DrillMode(final int energyCost, final float miningSpeed, final String translationKey, final ChatFormatting chatColor) {
            this.energyCost = energyCost;
            this.miningSpeed = miningSpeed;
            this.translationKey = translationKey;
            this.chatColor = chatColor;
        }

        public int getEnergyCost() {
            return energyCost;
        }

        public float getMiningSpeed() {
            return miningSpeed;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public ChatFormatting getChatColor() {
            return chatColor;
        }

        public DrillMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static DrillMode fromId(final int id) {
            DrillMode[] modes = values();
            if (id < 0 || id >= modes.length) {
                return NORMAL;
            }
            return modes[id];
        }
    }

    public DrillMode getMode(final ItemStack stack) {
        return DrillMode.fromId(stack.getOrCreateTag().getInt(DRILL_MODE_TAG));
    }

    public void setMode(final ItemStack stack, final DrillMode mode) {
        stack.getOrCreateTag().putInt(DRILL_MODE_TAG, mode.ordinal());
        syncSilkTouchEnchantment(stack);
    }

    private void syncSilkTouchEnchantment(final ItemStack stack) {
        Map<Enchantment, Integer> enchantments =
                new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchantments.remove(Enchantments.SILK_TOUCH);
        if (getMode(stack) == DrillMode.SILK_TOUCH && hasMiningEnergy(stack)) {
            enchantments.put(Enchantments.SILK_TOUCH, 1);
        }
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public int getEnergyCost(final ItemStack stack) {
        return getMode(stack).getEnergyCost();
    }

    @Override
    public double drawEnergy(final ItemStack stack, final double amount) {
        double drawn = super.drawEnergy(stack, amount);
        syncSilkTouchEnchantment(stack);
        return drawn;
    }

    public boolean hasMiningEnergy(final ItemStack stack) {
        return getStoredEnergy(stack) >= getEnergyCost(stack);
    }

    public boolean isEffectiveBlock(final BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                DrillMode next = getMode(stack).next();
                setMode(stack, next);
                player.displayClientMessage(
                        Component.translatable("item.ic2port.diamond_drill.mode_switch", Component.translatable(next.getTranslationKey()))
                                .withStyle(next.getChatColor()),
                        true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean canPerformAction(final ItemStack stack, final ToolAction toolAction) {
        if (!hasMiningEnergy(stack)) {
            return false;
        }
        return toolAction == ToolActions.PICKAXE_DIG || toolAction == ToolActions.SHOVEL_DIG;
    }

    @Override
    public float getDestroySpeed(final ItemStack stack, final BlockState state) {
        if (!hasMiningEnergy(stack) || !isEffectiveBlock(state)) {
            return HAND_SPEED;
        }
        return getMode(stack).getMiningSpeed();
    }

    @Override
    public boolean isCorrectToolForDrops(final ItemStack stack, final BlockState state) {
        if (!hasMiningEnergy(stack)) {
            return false;
        }
        if (!state.requiresCorrectToolForDrops()) {
            return true;
        }
        return isEffectiveBlock(state);
    }

    @Override
    public boolean mineBlock(
            final ItemStack stack,
            final Level level,
            final BlockState state,
            final BlockPos pos,
            final LivingEntity entity) {
        if (!level.isClientSide && isEffectiveBlock(state)) {
            drawEnergy(stack, getEnergyCost(stack));
        }
        return true;
    }

    @Override
    public <T extends LivingEntity> int damageItem(
            final ItemStack stack,
            final int amount,
            final T entity,
            final Consumer<T> onBroken) {
        return 0;
    }

    @Override
    public boolean isDamageable(final ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int stored = (int) Math.round(getStoredEnergy(stack));
        int max = (int) Math.round(getMaxEnergy());
        tooltip.add(Component.translatable("item.ic2port.diamond_drill.energy", stored, max)
                .withStyle(ChatFormatting.GRAY));
        DrillMode mode = getMode(stack);
        tooltip.add(Component.translatable("item.ic2port.diamond_drill.mode", Component.translatable(mode.getTranslationKey()))
                .withStyle(mode.getChatColor()));
        tooltip.add(Component.translatable("item.ic2port.diamond_drill.mode_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
