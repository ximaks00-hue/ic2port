package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.TradeOMatMenu;
import dev.ic2port.network.packet.TradeOMatActionPacket;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class TradeOMatScreen extends AbstractContainerScreen<TradeOMatMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/dispenser.png");

    public TradeOMatScreen(
            final TradeOMatMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = MachineMenuLayout.IMAGE_WIDTH;
        this.imageHeight = MachineMenuLayout.IMAGE_HEIGHT;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 176, 166);
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.menu.isBuyerView()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.ic2port.trade_o_mat.buyer_view"),
                    8, 20, 0x404040, false);
        }
        guiGraphics.drawString(this.font,
                Component.translatable("gui.ic2port.trade_o_mat.price", this.menu.getPrice()),
                8, 30, 0x404040, false);
        if (this.menu.canOwnerAdjustPrice()) {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.ic2port.trade_o_mat.price_hint"),
                    8, 50, 0x808080, false);
        }
        guiGraphics.drawString(this.font,
                Component.translatable(this.menu.hasLinkedChest()
                        ? "gui.ic2port.trade_o_mat.linked"
                        : "gui.ic2port.trade_o_mat.not_linked"),
                8, 40, 0x404040, false);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta) {
        if (this.menu.canOwnerAdjustPrice() && isHoveringPrice(mouseX, mouseY)) {
            int step = delta > 0 ? 1 : -1;
            if (hasShiftDown()) {
                step *= 10;
            }
            TradeOMatActionPacket.send(
                    this.menu.getBlockPos(),
                    TradeOMatActionPacket.ACTION_SET_PRICE,
                    Math.max(0, this.menu.getPrice() + step));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (this.minecraft != null
                && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            TradeOMatActionPacket.send(
                    this.menu.getBlockPos(),
                    TradeOMatActionPacket.ACTION_TOGGLE_BUYER_VIEW,
                    0);
            return true;
        }
        if (this.menu.canOwnerAdjustPrice()) {
            int delta = 0;
            if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_KP_ADD || keyCode == GLFW.GLFW_KEY_EQUAL) {
                delta = 1;
            } else if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT || keyCode == GLFW.GLFW_KEY_MINUS) {
                delta = -1;
            }
            if (delta != 0) {
                if (hasShiftDown()) {
                    delta *= 10;
                }
                if (hasControlDown()) {
                    delta *= 100;
                }
                TradeOMatActionPacket.send(
                        this.menu.getBlockPos(),
                        TradeOMatActionPacket.ACTION_SET_PRICE,
                        Math.max(0, this.menu.getPrice() + delta));
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isHoveringPrice(final double mouseX, final double mouseY) {
        int x = (int) (mouseX - this.leftPos);
        int y = (int) (mouseY - this.topPos);
        return x >= 8 && x < 120 && y >= 28 && y < 38;
    }
}
