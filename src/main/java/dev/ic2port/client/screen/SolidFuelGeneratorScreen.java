package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.SolidFuelGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client GUI for {@link dev.ic2port.blockentity.SolidFuelGeneratorBlockEntity}.
 */
public class SolidFuelGeneratorScreen extends AbstractContainerScreen<SolidFuelGeneratorMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public SolidFuelGeneratorScreen(
            final SolidFuelGeneratorMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
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

        if (this.menu.isBurning()) {
            int flameHeight = this.menu.getBurnProgressScaled(13);
            guiGraphics.blit(FURNACE_TEXTURE, x + 56, y + 36 + (13 - flameHeight), 176, 13 - flameHeight, 14, flameHeight + 1);
        }

        int energyHeight = this.menu.getEnergyScaled(13);
        if (energyHeight > 0) {
            guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34 + (13 - energyHeight), 176, 13 - energyHeight, 14, energyHeight + 1);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
