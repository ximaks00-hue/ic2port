package dev.ic2port.client.screen;

import dev.ic2port.menu.TeleportTubeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.entity.player.Inventory;

public class TeleportTubeScreen extends AbstractContainerScreen<TeleportTubeMenu> {

    public TeleportTubeScreen(final TeleportTubeMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("-"), button -> sendButton(0))
                .bounds(leftPos + 16, topPos + 20, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> sendButton(1))
                .bounds(leftPos + 140, topPos + 20, 20, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ic2port.teleport_tube.send"), button -> sendButton(2))
                .bounds(leftPos + 16, topPos + 48, 70, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.ic2port.teleport_tube.receive"), button -> sendButton(3))
                .bounds(leftPos + 90, topPos + 48, 70, 20).build());
    }

    private void sendButton(final int buttonId) {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.connection.send(new ServerboundContainerButtonClickPacket(menu.containerId, buttonId));
        }
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable(
                "gui.ic2port.teleport_tube.networkid",
                menu.getNetworkId()), 48, 26, 0x404040, false);
        guiGraphics.drawString(font, Component.translatable("container.inventory"), 8, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
