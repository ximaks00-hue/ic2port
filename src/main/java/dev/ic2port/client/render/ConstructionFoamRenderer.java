package dev.ic2port.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ic2port.blockentity.ConstructionFoamBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Renders obscurator camouflage on construction foam.
 */
public class ConstructionFoamRenderer implements BlockEntityRenderer<ConstructionFoamBlockEntity> {

    private final BlockEntityRendererProvider.Context context;

    public ConstructionFoamRenderer(final BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(
            final ConstructionFoamBlockEntity blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource bufferSource,
            final int packedLight,
            final int packedOverlay) {
        BlockState disguise = blockEntity.getDisguise();
        if (disguise == null || blockEntity.getLevel() == null) {
            return;
        }
        context.getBlockRenderDispatcher().renderSingleBlock(disguise, poseStack, bufferSource, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(final ConstructionFoamBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
