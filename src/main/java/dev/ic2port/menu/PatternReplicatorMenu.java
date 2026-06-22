package dev.ic2port.menu;

import dev.ic2port.blockentity.PatternReplicatorBlockEntity;
import dev.ic2port.item.UuMatterItem;
import dev.ic2port.setup.MenuTypeRegistry;
import dev.ic2port.util.MachineMenuLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class PatternReplicatorMenu extends AbstractContainerMenu {

    private final PatternReplicatorBlockEntity blockEntity;
    private final ContainerData data;

    public PatternReplicatorMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf), new SimpleContainerData(4));
    }

    public PatternReplicatorMenu(final int containerId, final Inventory playerInventory,
                                  final PatternReplicatorBlockEntity blockEntity, final ContainerData data) {
        super(MenuTypeRegistry.PATTERN_REPLICATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        IItemHandler handler = blockEntity.getItemHandler();
        this.addSlot(new SlotItemHandler(handler, PatternReplicatorBlockEntity.SLOT_PATTERN, 56, 17));
        this.addSlot(new SlotItemHandler(handler, PatternReplicatorBlockEntity.SLOT_UU_MATTER, 56, 53) {
            @Override public boolean mayPlace(final ItemStack stack) {
                return stack.getItem() instanceof UuMatterItem;
            }
        });
        this.addSlot(new SlotItemHandler(handler, PatternReplicatorBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override public boolean mayPlace(final ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory, col + row * 9 + 9, 8 + col * 18,
                        MachineMenuLayout.PLAYER_INVENTORY_START_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory, col, 8 + col * 18, MachineMenuLayout.HOTBAR_Y));
        }
        addDataSlots(data);
    }

    public boolean isCrafting() { return data.get(0) > 0; }
    public int getProgressScaled(final int width) {
        int p = data.get(0);
        int max = data.get(1);
        return (max > 0 && p > 0) ? p * width / max : 0;
    }
    public int getEnergyScaled(final int height) {
        int e = data.get(2);
        int max = data.get(3);
        return (max > 0 && e > 0) ? e * height / max : 0;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(final Player player) {
        return blockEntity != null
                && player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5, blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    private static PatternReplicatorBlockEntity getBlockEntity(final Inventory inv, final FriendlyByteBuf buf) {
        BlockEntity be = inv.player.level().getBlockEntity(buf.readBlockPos());
        if (be instanceof PatternReplicatorBlockEntity p) return p;
        throw new IllegalStateException("Expected PatternReplicatorBlockEntity");
    }
}
