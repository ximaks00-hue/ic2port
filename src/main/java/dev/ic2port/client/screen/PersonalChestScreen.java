package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.PersonalChestMenu;
import dev.ic2port.network.packet.PersonalStorageActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PersonalChestScreen extends AbstractContainerScreen<PersonalChestMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public PersonalChestScreen(
            final PersonalChestMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, 125);
        guiGraphics.blit(TEXTURE, x, y + 125, 0, 126, this.imageWidth, 97);
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.minecraft != null && this.minecraft.player != null && this.menu.isOwner(this.minecraft.player)) {
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.ic2port.personal_storage.friends", this.menu.getFriendCount()),
                    8, 6, 0x404040, false);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0 && this.minecraft != null && this.minecraft.player != null && this.menu.isOwner(this.minecraft.player)) {
            BlockPos pos = this.menu.getBlockPos();
            if (mouseX >= this.leftPos + 150 && mouseX < this.leftPos + 168
                    && mouseY >= this.topPos + 6 && mouseY < this.topPos + 14) {
                PersonalStorageActionPacket.send(pos, PersonalStorageActionPacket.ACTION_ADD_NEAREST);
                return true;
            }
            if (mouseX >= this.leftPos + 130 && mouseX < this.leftPos + 148
                    && mouseY >= this.topPos + 6 && mouseY < this.topPos + 14) {
                PersonalStorageActionPacket.send(pos, PersonalStorageActionPacket.ACTION_REMOVE_LAST);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(final GuiGraphics guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
