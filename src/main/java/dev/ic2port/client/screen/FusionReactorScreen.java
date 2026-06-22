package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.FusionReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FusionReactorScreen extends AbstractContainerScreen<FusionReactorMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public FusionReactorScreen(final FusionReactorMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (menu.isStructureValid()) {
            int heatHeight = menu.getHeatScaled(52);
            if (heatHeight > 0) {
                guiGraphics.blit(TEXTURE, x + 56, y + 36 + 52 - heatHeight, 176, 14, 14, heatHeight);
            }
            int lavaHeight = menu.getLavaScaled(52);
            if (lavaHeight > 0) {
                guiGraphics.blit(TEXTURE, x + 114, y + 36 + 52 - lavaHeight, 176, 31, 16, lavaHeight);
            }
            int energyHeight = menu.getEnergyScaled(52);
            if (energyHeight > 0) {
                guiGraphics.blit(TEXTURE, x + 80, y + 36 + 52 - energyHeight, 176, 14, 14, energyHeight);
            }
        }

        int statusColor = menu.isStructureValid() ? 0xFF55FF55 : 0xFFFF5555;
        guiGraphics.fill(x + 152, y + 18, x + 160, y + 26, statusColor);
        guiGraphics.fill(x + 153, y + 19, x + 159, y + 25, 0xFF202020);
        if (menu.isStructureValid()) {
            guiGraphics.fill(x + 154, y + 20, x + 158, y + 24, 0xFF55FF55);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component status = menu.isStructureValid()
                ? (menu.isHeated()
                        ? Component.translatable("gui.ic2port.fusion_reactor.running")
                        : Component.translatable("gui.ic2port.fusion_reactor.heating"))
                : Component.translatable("gui.ic2port.fusion_reactor.invalid");
        guiGraphics.drawString(this.font, status, 8, 52, menu.isStructureValid() ? 0x404040 : 0xFF5555, false);
        if (menu.isStructureValid()) {
            Component comparatorMode = menu.isComparatorHeatMode()
                    ? Component.translatable("gui.ic2port.fusion_reactor.comparator_heat")
                    : Component.translatable("gui.ic2port.fusion_reactor.comparator_lava");
            guiGraphics.drawString(this.font, comparatorMode, 8, 62, 0x606060, false);
            Component exportMode = menu.isAutoExportLava()
                    ? Component.translatable("gui.ic2port.fusion_reactor.auto_export_on")
                    : Component.translatable("gui.ic2port.fusion_reactor.auto_export_off");
            guiGraphics.drawString(this.font, exportMode, 8, 72, menu.isAutoExportLava() ? 0x55AA55 : 0xAA5555, false);
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

        if (isHovering(56, 36, 14, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.fusion_reactor.heat",
                            menu.getHeat(),
                            menu.getMaxHeat()),
                    mouseX,
                    mouseY);
        } else if (isHovering(80, 36, 14, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.fusion_reactor.energy",
                            menu.getStoredEnergy(),
                            menu.getMaxEnergy()),
                    mouseX,
                    mouseY);
        } else if (isHovering(114, 36, 16, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.fusion_reactor.lava",
                            menu.getLavaMb(),
                            menu.getMaxLavaMb()),
                    mouseX,
                    mouseY);
        } else if (isHovering(152, 18, 8, 8, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    menu.isStructureValid()
                            ? Component.translatable("gui.ic2port.fusion_reactor.structure_ok")
                            : Component.translatable("gui.ic2port.fusion_reactor.structure_bad"),
                    mouseX,
                    mouseY);
        }
    }
}
