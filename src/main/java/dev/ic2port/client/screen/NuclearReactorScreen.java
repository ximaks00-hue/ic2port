package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.NuclearReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NuclearReactorScreen extends AbstractContainerScreen<NuclearReactorMenu> {

    private static final ResourceLocation CONTAINER_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private static final ResourceLocation BAR_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public NuclearReactorScreen(
            final NuclearReactorMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(CONTAINER_TEXTURE, x, y, 0, 0, this.imageWidth, 6 * 18 + 17);
        guiGraphics.blit(CONTAINER_TEXTURE, x, y + 6 * 18 + 17, 0, 126, this.imageWidth, 96);

        RenderSystem.setShaderTexture(0, BAR_TEXTURE);

        int heatHeight = this.menu.getHeatScaled(52);
        if (heatHeight > 0) {
            guiGraphics.blit(BAR_TEXTURE, x + 152, y + 18 + 52 - heatHeight, 176, 52 - heatHeight, 16, heatHeight);
        }

        int energyHeight = this.menu.getEnergyScaled(52);
        if (energyHeight > 0) {
            guiGraphics.blit(BAR_TEXTURE, x + 134, y + 18 + 52 - energyHeight, 176, 14 + 52 - energyHeight, 16, energyHeight);
        }

        int statusColor = this.menu.isActive() ? 0xFF55FF55 : 0xFFFF5555;
        guiGraphics.fill(x + 116, y + 6, x + 124, y + 14, statusColor);
        guiGraphics.fill(x + 117, y + 7, x + 123, y + 13, 0xFF202020);
        if (this.menu.isActive()) {
            guiGraphics.fill(x + 118, y + 8, x + 122, y + 12, 0xFF55FF55);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component status = this.menu.isActive()
                ? Component.translatable("gui.ic2port.nuclear_reactor.active")
                : Component.translatable("gui.ic2port.nuclear_reactor.scram");
        guiGraphics.drawString(this.font, status, 128, 6, this.menu.isActive() ? 0x55FF55 : 0xFF5555, false);
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

        if (isHovering(134, 18, 16, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.nuclear_reactor.energy",
                            this.menu.getStoredEnergy(),
                            this.menu.getMaxEnergy()),
                    mouseX,
                    mouseY);
        } else if (isHovering(152, 18, 16, 52, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.nuclear_reactor.heat",
                            this.menu.getHeat(),
                            this.menu.getMaxHeat()),
                    mouseX,
                    mouseY);
        } else if (isHovering(116, 6, 8, 8, x, y)) {
            guiGraphics.renderTooltip(
                    this.font,
                    this.menu.isActive()
                            ? Component.translatable("gui.ic2port.nuclear_reactor.active_hint")
                            : Component.translatable("gui.ic2port.nuclear_reactor.scram_hint"),
                    mouseX,
                    mouseY);
        }
    }
}
