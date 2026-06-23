package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.menu.VillagerOMatMenu;
import dev.ic2port.util.VillagerOMatHelper;
import dev.ic2port.network.packet.VillagerOMatActionPacket;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class VillagerOMatScreen extends AbstractContainerScreen<VillagerOMatMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/dispenser.png");

    private static final int TRADE_BUTTON_X = 8;
    private static final int TRADE_BUTTON_Y = 52;
    private static final int TRADE_BUTTON_WIDTH = 70;
    private static final int TRADE_BUTTON_HEIGHT = 12;

    public VillagerOMatScreen(
            final VillagerOMatMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = MachineMenuLayout.IMAGE_WIDTH;
        this.imageHeight = MachineMenuLayout.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
    }

    @Override
    protected void renderBg(final GuiGraphics guiGraphics, final float partialTick, final int mouseX, final int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 176, 166);

        int energyHeight = this.menu.getEnergyScaled(40);
        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE, x + 8, y + 52 + (40 - energyHeight), 176, 52 - energyHeight, 8, energyHeight);
        }
        int cooldownWidth = this.menu.getCooldownScaled(60);
        if (cooldownWidth > 0) {
            guiGraphics.blit(TEXTURE, x + 100, y + 52, 176, 0, cooldownWidth, 4);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int villagers = this.menu.getVillagerCount();
        int selected = villagers == 0 ? 0 : this.menu.getSelectedVillagerIndex() + 1;
        guiGraphics.drawString(this.font,
                Component.translatable("gui.ic2port.villager_o_mat.villager", selected, villagers),
                8, 42, 0x404040, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.ic2port.villager_o_mat.xp", this.menu.getStoredXp()),
                100, 42, 0x404040, false);

        int tradeCount = this.menu.getTradeCount();
        for (int index = 0; index < VillagerOMatHelper.MAX_TRADES && index < 6; index++) {
            if (index >= tradeCount) {
                break;
            }
            int buttonY = TRADE_BUTTON_Y + index * (TRADE_BUTTON_HEIGHT + 2);
            boolean enabled = this.menu.isTradeEnabled(index);
            int color = enabled ? 0x00AA00 : 0xAA0000;
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.ic2port.villager_o_mat.trade", index + 1),
                    TRADE_BUTTON_X + 4, buttonY + 2, color, false);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (button == 0) {
            BlockPos pos = this.menu.getBlockEntity().getBlockPos();
            int localX = (int) mouseX - this.leftPos;
            int localY = (int) mouseY - this.topPos;

            if (localX >= 8 && localX < 28 && localY >= 42 && localY < 52) {
                VillagerOMatActionPacket.send(pos, VillagerOMatActionPacket.ACTION_PREV_VILLAGER, 0);
                return true;
            }
            if (localX >= 30 && localX < 50 && localY >= 42 && localY < 52) {
                VillagerOMatActionPacket.send(pos, VillagerOMatActionPacket.ACTION_NEXT_VILLAGER, 0);
                return true;
            }
            if (localX >= 160 && localX < 174 && localY >= 42 && localY < 52) {
                VillagerOMatActionPacket.send(pos, VillagerOMatActionPacket.ACTION_COLLECT_XP, 0);
                return true;
            }

            int tradeCount = this.menu.getTradeCount();
            for (int index = 0; index < tradeCount && index < 6; index++) {
                int buttonY = TRADE_BUTTON_Y + index * (TRADE_BUTTON_HEIGHT + 2);
                if (localX >= TRADE_BUTTON_X && localX < TRADE_BUTTON_X + TRADE_BUTTON_WIDTH
                        && localY >= buttonY && localY < buttonY + TRADE_BUTTON_HEIGHT) {
                    VillagerOMatActionPacket.send(pos, VillagerOMatActionPacket.ACTION_TOGGLE_TRADE, index);
                    return true;
                }
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
