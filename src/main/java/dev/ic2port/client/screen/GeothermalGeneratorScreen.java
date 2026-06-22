package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.GeothermalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

/**
 * Client GUI for {@link dev.ic2port.blockentity.GeothermalGeneratorBlockEntity}.
 */
public class GeothermalGeneratorScreen extends AbstractContainerScreen<GeothermalGeneratorMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");
    private static final ResourceLocation LAVA_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/lava_still.png");

    private static final int GAUGE_X = 30;
    private static final int GAUGE_Y = 17;
    private static final int GAUGE_WIDTH = 16;
    private static final int GAUGE_HEIGHT = 52;

    private static final int ENERGY_X = 116;
    private static final int ENERGY_Y = 17;
    private static final int ENERGY_HEIGHT = 52;

    public GeothermalGeneratorScreen(
            final GeothermalGeneratorMenu menu,
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
        RenderSystem.setShaderTexture(0, FURNACE_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(FURNACE_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        guiGraphics.fill(x + GAUGE_X, y + GAUGE_Y, x + GAUGE_X + GAUGE_WIDTH, y + GAUGE_Y + GAUGE_HEIGHT, 0xFF3A3A3A);

        int fluidHeight = this.menu.getFluidScaled(GAUGE_HEIGHT);
        if (fluidHeight > 0) {
            int fluidY = y + GAUGE_Y + (GAUGE_HEIGHT - fluidHeight);
            guiGraphics.fill(x + GAUGE_X + 1, fluidY, x + GAUGE_X + GAUGE_WIDTH - 1, y + GAUGE_Y + GAUGE_HEIGHT, 0xFFCC5500);
            RenderSystem.setShaderTexture(0, LAVA_TEXTURE);
            guiGraphics.blit(LAVA_TEXTURE, x + GAUGE_X + 2, fluidY, 0, 0, GAUGE_WIDTH - 4, fluidHeight, 16, 16);
        }

        int energyHeight = this.menu.getEnergyScaled(ENERGY_HEIGHT);
        if (energyHeight > 0) {
            guiGraphics.blit(
                    FURNACE_TEXTURE,
                    x + ENERGY_X,
                    y + ENERGY_Y + (ENERGY_HEIGHT - energyHeight),
                    176,
                    ENERGY_HEIGHT - energyHeight,
                    16,
                    energyHeight);
        }
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(GAUGE_X, GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderTooltip(
                    this.font,
                    Component.translatable(
                            "gui.ic2port.geothermal_generator.fluid",
                            formatAmount(this.menu.getFluidAmount()),
                            formatAmount(this.menu.getFluidCapacity())),
                    mouseX,
                    mouseY);
        }
    }

    private static String formatAmount(final int amount) {
        return String.format(Locale.US, "%,d", amount);
    }
}
