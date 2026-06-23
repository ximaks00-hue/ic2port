package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.InductionMatrixMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InductionMatrixScreen extends AbstractContainerScreen<InductionMatrixMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public InductionMatrixScreen(
            final InductionMatrixMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, FURNACE_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(FURNACE_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int energyHeight = this.menu.getEnergyScaled(52);
        if (energyHeight > 0) {
            guiGraphics.blit(FURNACE_TEXTURE, x + 8, y + 17 + (52 - energyHeight), 176, 52 - energyHeight, 16, energyHeight);
        }

        if (!this.menu.isStructureValid()) {
            guiGraphics.fill(x + 60, y + 14, x + 120, y + 74, 0x88FF0000);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (!this.menu.isStructureValid()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.ic2port.induction_matrix.invalid"), 62, 6, 0xFF5555, false);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;
        if (isHovering(8, 17, 16, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.induction_matrix.energy",
                            this.menu.getStoredEnergy(),
                            this.menu.getEnergyCapacity()),
                    mouseX,
                    mouseY);
        }
    }

    private boolean isHovering(final int x, final int y, final int width, final int height, final int mouseX, final int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
