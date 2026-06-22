package dev.ic2port.item;

import dev.ic2port.api.energy.EnergyTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Electric chestplate jetpack with Normal thrust and Hover descent modes.
 */
public class ElectricJetpackItem extends ElectricArmorItem {

    public static final double CAPACITY = 30000.0D;
    public static final String JETPACK_MODE_TAG = "JetpackMode";
    public static final double MIN_ACTIVE_ENERGY = 50.0D;

    public ElectricJetpackItem(final Properties properties) {
        super(ArmorMaterials.IRON, Type.CHESTPLATE, properties, CAPACITY, EnergyTier.LV);
    }

    public enum JetpackMode {
        NORMAL(50, "item.ic2port.electric_jetpack.mode.normal", ChatFormatting.GOLD),
        HOVER(20, "item.ic2port.electric_jetpack.mode.hover", ChatFormatting.AQUA);

        private final int energyPerTick;
        private final String translationKey;
        private final ChatFormatting chatColor;

        JetpackMode(final int energyPerTick, final String translationKey, final ChatFormatting chatColor) {
            this.energyPerTick = energyPerTick;
            this.translationKey = translationKey;
            this.chatColor = chatColor;
        }

        public int getEnergyPerTick() {
            return energyPerTick;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public ChatFormatting getChatColor() {
            return chatColor;
        }

        public JetpackMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static JetpackMode fromId(final int id) {
            JetpackMode[] modes = values();
            if (id < 0 || id >= modes.length) {
                return NORMAL;
            }
            return modes[id];
        }
    }

    public JetpackMode getMode(final ItemStack stack) {
        return JetpackMode.fromId(stack.getOrCreateTag().getInt(JETPACK_MODE_TAG));
    }

    public void setMode(final ItemStack stack, final JetpackMode mode) {
        stack.getOrCreateTag().putInt(JETPACK_MODE_TAG, mode.ordinal());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                JetpackMode next = getMode(stack).next();
                setMode(stack, next);
                player.displayClientMessage(
                        Component.empty()
                                .append(Component.translatable("item.ic2port.electric_jetpack.mode_switch_prefix")
                                        .withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable(next.getTranslationKey())
                                        .withStyle(next.getChatColor())),
                        true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(
            final ItemStack stack,
            final @Nullable Level level,
            final List<Component> tooltip,
            final TooltipFlag flag) {
        int stored = (int) Math.round(getStoredEnergy(stack));
        int max = (int) Math.round(getMaxEnergy());
        tooltip.add(Component.translatable("item.ic2port.electric_jetpack.energy", stored, max)
                .withStyle(ChatFormatting.GRAY));
        JetpackMode mode = getMode(stack);
        tooltip.add(Component.translatable("item.ic2port.electric_jetpack.mode", Component.translatable(mode.getTranslationKey()))
                .withStyle(mode.getChatColor()));
        tooltip.add(Component.translatable("item.ic2port.electric_jetpack.mode_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
