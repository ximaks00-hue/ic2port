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
 * MV electric mining tool with Normal, Silk Touch and Fortune modes.
 */
public class AdvancedDrillItem extends ElectricItem {

    public static final double CAPACITY = 150_000.0D;
    public static final String DRILL_MODE_TAG = "DrillMode";

    private static final float HAND_SPEED = 1.0F;
    private static final float MINING_SPEED = 26.0F;

    public AdvancedDrillItem(final Properties properties) {
        super(properties.stacksTo(1), CAPACITY, EnergyTier.MV);
    }

    public enum DrillMode {
        NORMAL(100, "item.ic2port.advanced_drill.mode.normal", ChatFormatting.GREEN),
        SILK_TOUCH(250, "item.ic2port.advanced_drill.mode.silk_touch", ChatFormatting.AQUA),
        FORTUNE(400, "item.ic2port.advanced_drill.mode.fortune", ChatFormatting.GOLD);

        private final int energyCost;
        private final String translationKey;
        private final ChatFormatting chatColor;

        DrillMode(final int energyCost, final String translationKey, final ChatFormatting chatColor) {
            this.energyCost = energyCost;
            this.translationKey = translationKey;
            this.chatColor = chatColor;
        }

        public int getEnergyCost() {
            return energyCost;
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
        syncModeEnchantments(stack);
    }

    public void cycleMode(final ItemStack stack) {
        setMode(stack, getMode(stack).next());
    }

    private void syncModeEnchantments(final ItemStack stack) {
        Map<Enchantment, Integer> enchantments =
                new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchantments.remove(Enchantments.SILK_TOUCH);
        enchantments.remove(Enchantments.BLOCK_FORTUNE);

        if (hasMiningEnergy(stack)) {
            switch (getMode(stack)) {
                case SILK_TOUCH -> enchantments.put(Enchantments.SILK_TOUCH, 1);
                case FORTUNE -> enchantments.put(Enchantments.BLOCK_FORTUNE, 3);
                default -> {
                }
            }
        }

        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public int getEnergyCost(final ItemStack stack) {
        return getMode(stack).getEnergyCost();
    }

    @Override
    public double drawEnergy(final ItemStack stack, final double amount) {
        double drawn = super.drawEnergy(stack, amount);
        syncModeEnchantments(stack);
        return drawn;
    }

    public boolean hasMiningEnergy(final ItemStack stack) {
        return getStoredEnergy(stack) >= getEnergyCost(stack);
    }

    public boolean isEffectiveBlock(final BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Nullable
    public static ItemStack findHeldDrill(final Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof AdvancedDrillItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof AdvancedDrillItem) {
            return off;
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof AdvancedDrillItem)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                cycleMode(stack);
                notifyModeSwitch(player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void notifyModeSwitch(final Player player, final ItemStack stack) {
        if (!(stack.getItem() instanceof AdvancedDrillItem drill)) {
            return;
        }
        DrillMode mode = drill.getMode(stack);
        player.displayClientMessage(
                Component.translatable(
                                "item.ic2port.advanced_drill.mode_switch",
                                Component.translatable(mode.getTranslationKey()))
                        .withStyle(mode.getChatColor()),
                true);
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
        return MINING_SPEED;
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
            syncHolderInventory(entity);
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
        tooltip.add(Component.translatable("item.ic2port.advanced_drill.energy", stored, max)
                .withStyle(ChatFormatting.GRAY));
        DrillMode mode = getMode(stack);
        tooltip.add(Component.translatable(
                        "item.ic2port.advanced_drill.mode",
                        Component.translatable(mode.getTranslationKey()))
                .withStyle(mode.getChatColor()));
        tooltip.add(Component.translatable(
                        "item.ic2port.advanced_drill.energy_cost",
                        mode.getEnergyCost())
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.ic2port.advanced_drill.mode_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
