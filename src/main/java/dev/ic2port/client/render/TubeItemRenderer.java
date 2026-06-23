package dev.ic2port.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.ic2port.blockentity.TubeBlockEntity;
import dev.ic2port.tube.TransportedItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Renders animated items travelling inside tube blocks.
 */
public class TubeItemRenderer implements BlockEntityRenderer<TubeBlockEntity> {

    public TubeItemRenderer(final BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            final TubeBlockEntity tube,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource buffer,
            final int packedLight,
            final int packedOverlay) {
        if (tube.getInFlightItems().isEmpty()) {
            return;
        }
        long gameTime = tube.getLevel() != null ? tube.getLevel().getGameTime() : 0L;
        for (TransportedItem item : tube.getInFlightItems()) {
            ItemStack stack = item.getStack();
            if (stack.isEmpty()) {
                continue;
            }
            Direction travel = item.getExportDirection() != null
                    ? item.getExportDirection()
                    : oppositeOrDefault(item.getEntryDirection());
            float offset = (item.getProgress() + (item.getSpeed() * partialTick)) / 100.0F - 0.5F;

            poseStack.pushPose();
            poseStack.translate(
                    0.5D + travel.getStepX() * offset,
                    0.5D + travel.getStepY() * offset,
                    0.5D + travel.getStepZ() * offset);
            poseStack.scale(0.35F, 0.35F, 0.35F);
            poseStack.mulPose(Axis.YP.rotationDegrees((gameTime + partialTick) * 3.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    tube.getLevel(),
                    0);
            poseStack.popPose();
        }
    }

    private static Direction oppositeOrDefault(@Nullable final Direction entry) {
        return entry != null ? entry.getOpposite() : Direction.NORTH;
    }
}
