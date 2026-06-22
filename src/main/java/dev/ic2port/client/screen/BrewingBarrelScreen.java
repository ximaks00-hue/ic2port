package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.brewing.BrewType;
import dev.ic2port.menu.BrewingBarrelMenu;
import dev.ic2port.util.WhiskyHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Client GUI for {@link dev.ic2port.blockentity.BrewingBarrelBlockEntity}.
 */
public class BrewingBarrelScreen extends AbstractContainerScreen<BrewingBarrelMenu> {

    private static final ResourceLocation BREWING_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/brewing_stand.png");

    public BrewingBarrelScreen(
            final BrewingBarrelMenu menu,
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
        RenderSystem.setShaderTexture(0, BREWING_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(BREWING_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isBrewing()) {
            int progress = this.menu.getBrewProgressScaled(28);
            guiGraphics.blit(BREWING_TEXTURE, x + 97, y + 16, 176, 0, 9, progress);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.menu.getTemperature() + "°C", 8, 52, 0x404040, false);
        if (this.menu.isBrewing() || this.menu.getBrewType() != BrewType.NONE) {
            String key = "brewing.ic2port.type." + this.menu.getBrewType().name().toLowerCase();
            guiGraphics.drawString(this.font, Component.translatable(key), 8, 62, 0x404040, false);
            if (this.menu.getBrewType() == BrewType.WHISKY && this.menu.isBrewing()) {
                int years = WhiskyHelper.yearsFromProgress(this.menu.getBrewProgress());
                guiGraphics.drawString(
                        this.font,
                        Component.translatable("brewing.ic2port.whisky_years", years, WhiskyHelper.MAX_YEARS),
                        8,
                        72,
                        0x404040,
                        false);
            }
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
