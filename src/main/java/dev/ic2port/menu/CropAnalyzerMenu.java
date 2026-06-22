package dev.ic2port.menu;

import dev.ic2port.blockentity.CropAnalyzerBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.StationaryCropAnalyzerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CropAnalyzerMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final CropAnalyzerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CropAnalyzerMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(6));
    }

    public CropAnalyzerMenu(
            final int containerId,
            final Inventory playerInventory,
            final CropAnalyzerBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.CROP_ANALYZER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int max = data.get(1);
        return max <= 0 ? 0 : data.get(0) * height / max;
    }

    public int getScanScaled(final int width) {
        int max = data.get(3);
        return max <= 0 ? 0 : data.get(2) * width / max;
    }

    public int getStoredEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    public int getScanProgress() {
        return data.get(2);
    }

    public int getScanProgressMax() {
        return data.get(3);
    }

    public boolean hasTarget() {
        return data.get(4) != 0;
    }

    public int getTargetScanLevel() {
        return data.get(5);
    }

    public static int getScanProgressMaxValue() {
        return (int) StationaryCropAnalyzerHelper.ENERGY_PER_SCAN_LEVEL;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && level.getBlockEntity(blockEntity.getBlockPos()) == blockEntity
                && player.distanceToSqr(
                        blockEntity.getBlockPos().getX() + 0.5D,
                        blockEntity.getBlockPos().getY() + 0.5D,
                        blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    HOTBAR_Y));
        }
    }

    private static CropAnalyzerBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof CropAnalyzerBlockEntity analyzer) {
            return analyzer;
        }
        throw new IllegalStateException("Expected crop analyzer block entity");
    }
}
