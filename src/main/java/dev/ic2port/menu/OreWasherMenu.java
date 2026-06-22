package dev.ic2port.menu;

import dev.ic2port.blockentity.OreWasherBlockEntity;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class OreWasherMenu extends MachineWithUpgradesMenu {

    private final OreWasherBlockEntity blockEntity;
    private final ContainerData data;

    public OreWasherMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf), new SimpleContainerData(4));
    }

    public OreWasherMenu(final int containerId, final Inventory playerInventory,
                          final OreWasherBlockEntity blockEntity, final ContainerData data) {
        super(MenuTypeRegistry.ORE_WASHER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();
        this.addSlot(createProcessInputSlot(handler, OreWasherBlockEntity.SLOT_INPUT,
                MachineMenuLayout.SLOT_INPUT_X, MachineMenuLayout.SLOT_INPUT_Y));
        this.addSlot(new SlotItemHandler(handler, OreWasherBlockEntity.SLOT_OUTPUT,
                MachineMenuLayout.SLOT_OUTPUT_X, MachineMenuLayout.SLOT_OUTPUT_Y) {
            @Override public boolean mayPlace(final ItemStack stack) { return false; }
        });
        addUpgradeSlots(handler);
        addPlayerInventoryAndHotbar(playerInventory);
        addDataSlots(data);
    }

    public boolean isCrafting() { return isMachineCrafting(data); }
    public int getProgressScaled(final int width) { return getMachineProgressScaled(data, width); }
    public int getEnergyScaled(final int height) { return getMachineEnergyScaled(data, height); }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return quickMoveMachineStack(player, index);
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5, blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    private void addPlayerInventoryAndHotbar(final Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        inv, col + row * 9 + 9, 8 + col * 18,
                        MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    inv, col, 8 + col * 18, MachineMenuLayout.HOTBAR_Y));
        }
    }

    private static OreWasherBlockEntity getBlockEntity(final Inventory inv, final FriendlyByteBuf buf) {
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof OreWasherBlockEntity o) return o;
        throw new IllegalStateException("Expected OreWasherBlockEntity");
    }
}
