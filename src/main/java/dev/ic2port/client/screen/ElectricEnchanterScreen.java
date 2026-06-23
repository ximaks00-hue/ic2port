package dev.ic2port.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ic2port.blockentity.ElectricEnchanterBlockEntity;
import dev.ic2port.menu.ElectricEnchanterMenu;
import dev.ic2port.network.packet.ElectricEnchanterActionPacket;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ElectricEnchanterScreen extends AbstractContainerScreen<ElectricEnchanterMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/enchanting_table.png");

    public ElectricEnchanterScreen(
            final ElectricEnchanterMenu menu,
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

        int energyHeight = this.menu.getEnergyScaled(52);
        if (energyHeight > 0) {
            guiGraphics.blit(TEXTURE, this.leftPos + 8, this.topPos + 17 + (52 - energyHeight),
                    176, 52 - energyHeight, 16, energyHeight);
        }
    }

    @Override
    protected void renderLabels(final GuiGraphics guiGraphics, final int mouseX, final int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component tab = this.menu.getActiveTab() == ElectricEnchanterBlockEntity.TAB_DISENCHANT
                ? Component.translatable("gui.ic2port.electric_enchanter.disenchant")
                : Component.translatable("gui.ic2port.electric_enchanter.enchant");
        guiGraphics.drawString(this.font, tab, 8, 20, 0x404040, false);
        guiGraphics.drawString(this.font,
                Component.translatable("gui.ic2port.electric_enchanter.xp_stored", this.menu.getStoredXp()),
                8, 30, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        BlockPos pos = this.menu.getBlockPos();
        if (pos == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 120 && mouseX < x + 168 && mouseY >= y + 6 && mouseY < y + 16) {
            ElectricEnchanterActionPacket.send(pos, ElectricEnchanterActionPacket.ACTION_SET_TAB,
                    this.menu.getActiveTab() == ElectricEnchanterBlockEntity.TAB_ENCHANT
                            ? ElectricEnchanterBlockEntity.TAB_DISENCHANT
                            : ElectricEnchanterBlockEntity.TAB_ENCHANT);
            return true;
        }
        if (mouseX >= x + 100 && mouseX < x + 118 && mouseY >= y + 52 && mouseY < y + 64) {
            ElectricEnchanterActionPacket.send(pos, ElectricEnchanterActionPacket.ACTION_ENCHANT, 0);
            return true;
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
