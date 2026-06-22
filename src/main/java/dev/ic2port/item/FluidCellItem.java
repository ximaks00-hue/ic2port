package dev.ic2port.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Universal IC2-style fluid cell. Stores 1000 mB of any fluid in NBT.
 * Empty cells are stackable; filled cells are not.
 */
public class FluidCellItem extends Item {

    public static final int CAPACITY_MB = 1000;
    private static final String FLUID_TAG = "Fluid";
    private static final String AMOUNT_TAG = "Amount";

    public FluidCellItem(final Properties properties) {
        super(properties);
    }

    public static boolean isEmpty(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FLUID_TAG)) {
            return true;
        }
        return tag.getInt(AMOUNT_TAG) <= 0;
    }

    @Nullable
    public static Fluid getFluid(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FLUID_TAG)) {
            return null;
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString(FLUID_TAG)));
        return (fluid == null || fluid == Fluids.EMPTY) ? null : fluid;
    }

    public static int getAmount(final ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt(AMOUNT_TAG);
    }

    public static ItemStack createFilled(final Item cellItem, final Fluid fluid, final int amount) {
        ItemStack stack = new ItemStack(cellItem);
        CompoundTag tag = stack.getOrCreateTag();
        ResourceLocation key = ForgeRegistries.FLUIDS.getKey(fluid);
        tag.putString(FLUID_TAG, key != null ? key.toString() : Fluids.EMPTY.toString());
        tag.putInt(AMOUNT_TAG, amount);
        return stack;
    }

    public static ItemStack fillCell(final ItemStack emptyCell, final Fluid fluid) {
        return createFilled(emptyCell.getItem(), fluid, CAPACITY_MB);
    }

    public static ItemStack emptyCell(final Item cellItem) {
        return new ItemStack(cellItem);
    }

    @Override
    public int getMaxStackSize(final ItemStack stack) {
        return isEmpty(stack) ? 64 : 1;
    }

    @Override
    public void appendHoverText(final ItemStack stack, final @Nullable Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        if (isEmpty(stack)) {
            tooltip.add(Component.translatable("item.ic2port.fluid_cell.empty").withStyle(ChatFormatting.GRAY));
        } else {
            Fluid fluid = getFluid(stack);
            String name = fluid != null
                    ? fluid.defaultFluidState().createLegacyBlock().getBlock().getName().getString()
                    : "Unknown";
            tooltip.add(Component.translatable("item.ic2port.fluid_cell.filled", name, getAmount(stack))
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
