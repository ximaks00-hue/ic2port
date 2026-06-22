package dev.ic2port.client.screen;

import dev.ic2port.menu.MassFabricatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MassFabricatorScreen extends MachineScreen<MassFabricatorMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public MassFabricatorScreen(
            final MassFabricatorMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        if (this.menu.isFabricating()) {
            int progressWidth = this.menu.getFabricationProgressScaled(24);
            guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34, 176, 14, progressWidth + 1, 16);
        }
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
