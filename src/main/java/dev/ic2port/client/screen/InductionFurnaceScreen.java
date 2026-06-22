package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.InductionFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InductionFurnaceScreen extends MachineScreen<InductionFurnaceMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public InductionFurnaceScreen(
            final InductionFurnaceMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        RenderSystem.setShaderTexture(0, FURNACE_TEXTURE);
        guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 26, 176, 14, this.menu.getProgressAScaled(24) + 1, 16);
        guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 44, 176, 14, this.menu.getProgressBScaled(24) + 1, 16);
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
