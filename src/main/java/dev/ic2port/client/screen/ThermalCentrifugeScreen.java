package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.blockentity.ThermalCentrifugeBlockEntity;
import dev.ic2port.menu.ThermalCentrifugeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ThermalCentrifugeScreen extends MachineScreen<ThermalCentrifugeMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public ThermalCentrifugeScreen(
            final ThermalCentrifugeMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        if (this.menu.isCrafting() && !this.menu.isHeating()) {
            int progressWidth = this.menu.getProcessedProgressScaled(24);
            guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34, 176, 14, progressWidth + 1, 16);
        }

        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));

        int rotorHeight = this.menu.getRotorHeatScaled(13);
        if (rotorHeight > 0) {
            RenderSystem.setShaderTexture(0, FURNACE_TEXTURE);
            guiGraphics.blit(FURNACE_TEXTURE, x + 38, y + 36 + (13 - rotorHeight), 176, 0, 14, rotorHeight + 1);
        }
    }
}
