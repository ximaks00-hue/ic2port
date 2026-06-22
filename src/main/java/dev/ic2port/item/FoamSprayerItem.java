package dev.ic2port.item;



import dev.ic2port.setup.ItemRegistry;

import dev.ic2port.util.FoamSprayHelper;

import net.minecraft.world.InteractionHand;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.InteractionResultHolder;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Item;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;



/**

 * Sprays wet construction foam using foam pellets (IC2 CF sprayer).

 */

public class FoamSprayerItem extends Item {



    public static final int MAX_BLOCKS_PER_USE = 5;



    public FoamSprayerItem(final Properties properties) {

        super(properties.durability(128));

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

                MAX_BLOCKS_PER_USE,

                FoamSprayerItem::consumePellet);

        return placed > 0 ? InteractionResult.CONSUME : InteractionResult.PASS;

    }



    @Override

    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {

            return InteractionResultHolder.success(stack);

        }

        int placed = FoamSprayHelper.sprayFromLook(

                level,

                player,

                stack,

                hand,

                MAX_BLOCKS_PER_USE,

                FoamSprayerItem::consumePellet);

        return placed > 0 ? InteractionResultHolder.consume(stack) : InteractionResultHolder.pass(stack);

    }



    private static boolean consumePellet(final Player player) {

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {

            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.is(ItemRegistry.FOAM_PELLET.get())) {

                if (!player.getAbilities().instabuild) {

                    stack.shrink(1);

                }

                return true;

            }

        }

        return false;

    }

}


