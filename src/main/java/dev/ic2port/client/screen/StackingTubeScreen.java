package dev.ic2port.client.screen;

import dev.ic2port.menu.StackingTubeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;

public class StackingTubeScreen extends AbstractContainerScreen<StackingTubeMenu> {

    public StackingTubeScreen(final StackingTubeMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.ic2port.stacking_tube.extract"), button -> {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.connection.send(new ServerboundContainerButtonClickPacket(menu.containerId, 0));
            }
        }).bounds(leftPos + 56, topPos + 20, 64, 20).build());
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable(
                "gui.ic2port.stacking_tube.stored",
                menu.getStoredCount()), 8, 52, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable(
                "gui.ic2port.stacking_tube.threshold",
                menu.getThreshold()), 8, 64, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("container.inventory"), 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
