package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.OreScannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class OreScannerScreen extends AbstractContainerScreen<OreScannerMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public OreScannerScreen(final OreScannerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        addRenderableWidget(Button.builder(Component.translatable("gui.ic2port.ore_scanner.scan"), button -> {
            if (minecraft != null && minecraft.player != null) {
                menu.triggerScan(minecraft.player);
            }
        }).bounds(leftPos + 116, topPos + 20, 50, 20).build());
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int energyHeight = this.menu.getEnergyScaled(52);
        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE, x + 56, y + 17 + (52 - energyHeight), 176, 52 - energyHeight, 16, energyHeight);
        }

        int scanWidth = this.menu.getScanScaled(50);
        if (scanWidth > 0) {
            guiGraphics.fill(x + 116, y + 45, x + 116 + scanWidth, y + 50, 0xFF00AA00);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
