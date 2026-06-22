package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.AutoFeederMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AutoFeederScreen extends AbstractContainerScreen<AutoFeederMenu> {

    private static final ResourceLocation CONTAINER_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public AutoFeederScreen(final AutoFeederMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(CONTAINER_TEXTURE, x, y, 0, 0, this.imageWidth, 3 * 18 + 17);
        guiGraphics.blit(CONTAINER_TEXTURE, x, y + 3 * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
