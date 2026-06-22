package dev.ic2port.item;

import dev.ic2port.block.BatBoxBlock;
import dev.ic2port.block.EsuBlock;
import dev.ic2port.block.EVTransformerBlock;
import dev.ic2port.block.LVTransformerBlock;
import dev.ic2port.block.MFEBlock;
import dev.ic2port.block.MFSUBlock;
import dev.ic2port.block.MVTransformerBlock;
import dev.ic2port.block.NuclearReactorBlock;
import dev.ic2port.block.SolidFuelGeneratorBlock;
import dev.ic2port.block.GeothermalGeneratorBlock;
import dev.ic2port.block.WindMillBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class WrenchItem extends Item {

    public WrenchItem(final Properties properties) {
        super(properties.durability(256));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        DirectionProperty facingProperty = getFacingProperty(state);
        if (facingProperty == null) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer() != null
                && context.getPlayer().blockActionRestricted(level, pos, GameType.SURVIVAL)) {
            return InteractionResult.FAIL;
        }

        Direction clickedFace = context.getClickedFace();
        if (!facingProperty.getPossibleValues().contains(clickedFace)) {
            return InteractionResult.PASS;
        }
        if (state.getValue(facingProperty) == clickedFace) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(facingProperty, clickedFace), 3);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS,
                    0.6F,
                    1.2F);
            if (context.getPlayer() != null) {
                context.getPlayer().getItemInHand(context.getHand()).hurtAndBreak(
                        1,
                        context.getPlayer(),
                        player -> player.broadcastBreakEvent(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private static DirectionProperty getFacingProperty(final BlockState state) {
        if (state.hasProperty(BatBoxBlock.FACING)) {
            return BatBoxBlock.FACING;
        }
        if (state.hasProperty(MFEBlock.FACING)) {
            return MFEBlock.FACING;
        }
        if (state.hasProperty(MFSUBlock.FACING)) {
            return MFSUBlock.FACING;
        }
        if (state.hasProperty(EsuBlock.FACING)) {
            return EsuBlock.FACING;
        }
        if (state.hasProperty(NuclearReactorBlock.FACING)) {
            return NuclearReactorBlock.FACING;
        }
        if (state.hasProperty(LVTransformerBlock.FACING)) {
            return LVTransformerBlock.FACING;
        }
        if (state.hasProperty(MVTransformerBlock.FACING)) {
            return MVTransformerBlock.FACING;
        }
        if (state.hasProperty(EVTransformerBlock.FACING)) {
            return EVTransformerBlock.FACING;
        }
        if (state.hasProperty(SolidFuelGeneratorBlock.FACING)) {
            return SolidFuelGeneratorBlock.FACING;
        }
        if (state.hasProperty(GeothermalGeneratorBlock.FACING)) {
            return GeothermalGeneratorBlock.FACING;
        }
        if (state.hasProperty(WindMillBlock.FACING)) {
            return WindMillBlock.FACING;
        }
        return null;
    }
}
