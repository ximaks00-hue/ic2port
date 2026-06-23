package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.MinerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MinerScreen extends AbstractContainerScreen<MinerMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public MinerScreen(final MinerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
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
        if (isHovering(38, 17, 18, 18, x, y)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ic2port.miner.drill_slot"), mouseX, mouseY);
        } else if (isHovering(56, 17, 18, 18, x, y)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ic2port.miner.scanner_slot"), mouseX, mouseY);
        } else if (isHovering(74, 17, 18, 18, x, y)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ic2port.miner.pipe_slot"), mouseX, mouseY);
        }
        if (this.menu.isDone()) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.ic2port.miner.done_hint"), mouseX, mouseY);
        }
    }
}
