package dev.ic2port.client.screen;

import dev.ic2port.menu.VacuumCannerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class VacuumCannerScreen extends MachineScreen<VacuumCannerMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public VacuumCannerScreen(final VacuumCannerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        if (this.menu.isCrafting()) {
            int progressHeight = this.menu.getProcessedProgressScaled(16);
            guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34 + (16 - progressHeight), 176, 14, 24, progressHeight);
        }
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
