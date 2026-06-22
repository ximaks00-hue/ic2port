package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.CropmatronMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CropmatronScreen extends AbstractContainerScreen<CropmatronMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public CropmatronScreen(final CropmatronMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int energyHeight = menu.getEnergyScaled(52);
        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE, x + 56, y + 17 + 52 - energyHeight, 176, 14, 14, energyHeight);
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
        if (isHovering(56, 17, 14, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.cropmatron.energy",
                            menu.getStoredEnergy(),
                            menu.getMaxEnergy()),
                    mouseX,
                    mouseY);
        } else if (isHovering(80, 35, 16, 16, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable("gui.ic2port.cropmatron.supply_hint"),
                    mouseX,
                    mouseY);
        }
    }
}
