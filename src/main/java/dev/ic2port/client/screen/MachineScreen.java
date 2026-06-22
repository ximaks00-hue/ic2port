package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Shared background rendering for machine screens with upgrade slots.
 */
public abstract class MachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    protected MachineScreen(final T menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = MachineMenuLayout.IMAGE_WIDTH;
        this.imageHeight = MachineMenuLayout.IMAGE_HEIGHT;
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
        guiGraphics.blit(FURNACE_TEXTURE, x, y, 0, 0, 176, this.imageHeight);

        guiGraphics.fill(
                x + MachineMenuLayout.UPGRADE_PANEL_X,
                y + MachineMenuLayout.UPGRADE_PANEL_Y,
                x + MachineMenuLayout.UPGRADE_PANEL_X + MachineMenuLayout.UPGRADE_PANEL_WIDTH,
                y + MachineMenuLayout.UPGRADE_PANEL_Y + MachineMenuLayout.UPGRADE_PANEL_HEIGHT,
                0xFF8B8B8B);
        guiGraphics.fill(
                x + MachineMenuLayout.UPGRADE_PANEL_X + 1,
                y + MachineMenuLayout.UPGRADE_PANEL_Y + 1,
                x + MachineMenuLayout.UPGRADE_PANEL_X + MachineMenuLayout.UPGRADE_PANEL_WIDTH - 1,
                y + MachineMenuLayout.UPGRADE_PANEL_Y + MachineMenuLayout.UPGRADE_PANEL_HEIGHT - 1,
                0xFFC6C6C6);

        renderMachineOverlay(guiGraphics, x, y);
    }

    protected abstract void renderMachineOverlay(GuiGraphics guiGraphics, int x, int y);

    protected void renderProgressBar(final GuiGraphics guiGraphics, final int x, final int y, final int progressWidth) {
        guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34, 176, 14, progressWidth + 1, 16);
    }

    protected void renderEnergyBar(final GuiGraphics guiGraphics, final int x, final int y, final int energyHeight) {
        if (energyHeight > 0) {
            guiGraphics.blit(
                    FURNACE_TEXTURE,
                    x + 56,
                    y + 36 + (13 - energyHeight),
                    176,
                    13 - energyHeight,
                    14,
                    energyHeight + 1);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
