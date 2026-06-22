package dev.ic2port.menu;

import dev.ic2port.blockentity.ElectricFurnaceBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ElectricFurnaceMenu extends MachineWithUpgradesMenu {

    private final ElectricFurnaceBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public ElectricFurnaceMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(4));
    }

    public ElectricFurnaceMenu(
            final int containerId,
            final Inventory playerInventory,
            final ElectricFurnaceBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.ELECTRIC_FURNACE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();
        this.addSlot(createProcessInputSlot(
                itemHandler,
                ElectricFurnaceBlockEntity.SLOT_INPUT,
                MachineMenuLayout.SLOT_INPUT_X,
                MachineMenuLayout.SLOT_INPUT_Y));
        this.addSlot(new SlotItemHandler(itemHandler, ElectricFurnaceBlockEntity.SLOT_OUTPUT, MachineMenuLayout.SLOT_OUTPUT_X, MachineMenuLayout.SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }
        });

        addUpgradeSlots(itemHandler);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getProcessedProgressScaled(final int width) {
        return getMachineProgressScaled(data, width);
    }

    public int getEnergyScaled(final int height) {
        return getMachineEnergyScaled(data, height);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return quickMoveMachineStack(player, index);
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.blockEntity != null
                && this.level.getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity
                && player.distanceToSqr(
                this.blockEntity.getBlockPos().getX() + 0.5D,
                this.blockEntity.getBlockPos().getY() + 0.5D,
                this.blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    private void addPlayerInventory(final Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(final Inventory playerInventory) {
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    MachineMenuLayout.HOTBAR_Y
            ));
        }
    }

    private static ElectricFurnaceBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof ElectricFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected ElectricFurnaceBlockEntity at provided BlockPos");
    }
}
