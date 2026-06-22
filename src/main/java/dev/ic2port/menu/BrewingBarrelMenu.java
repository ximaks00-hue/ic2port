package dev.ic2port.menu;

import dev.ic2port.brewing.BrewType;
import dev.ic2port.blockentity.BrewingBarrelBlockEntity;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.setup.MenuTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Container for the IC2-style brewing barrel.
 */
public class BrewingBarrelMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private final BrewingBarrelBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public BrewingBarrelMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(5));
    }

    public BrewingBarrelMenu(
            final int containerId,
            final Inventory playerInventory,
            final BrewingBarrelBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.BREWING_BARREL_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler handler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(handler, BrewingBarrelBlockEntity.SLOT_HOPS, 49, 20) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.is(ItemRegistry.HOPS.get())
                        || stack.is(Items.SUGAR_CANE)
                        || stack.is(Items.REDSTONE);
            }
        });
        this.addSlot(new SlotItemHandler(handler, BrewingBarrelBlockEntity.SLOT_WHEAT, 49, 38) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.is(Items.WHEAT) || stack.is(Items.GLOWSTONE_DUST);
            }
        });
        this.addSlot(new SlotItemHandler(handler, BrewingBarrelBlockEntity.SLOT_WATER, 49, 56) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.is(Items.WATER_BUCKET);
            }
        });
        this.addSlot(new SlotItemHandler(handler, BrewingBarrelBlockEntity.SLOT_OUTPUT, 115, 38) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public boolean isBrewing() {
        return data.get(2) > 0;
    }

    public int getBrewProgressScaled(final int width) {
        int progress = data.get(0);
        int total = data.get(1);
        if (total <= 0 || progress <= 0) {
            return 0;
        }
        return progress * width / total;
    }

    public int getBrewProgress() {
        return data.get(0);
    }

    public BrewType getBrewType() {
        return BrewType.fromIndex(data.get(4));
    }

    public int getTemperature() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        var slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        quickMoved = source.copy();

        if (index == BrewingBarrelBlockEntity.SLOT_OUTPUT) {
            if (!moveItemStackTo(source, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 4) {
            if (!moveItemStackTo(source, 4, 40, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(ItemRegistry.HOPS.get()) || source.is(Items.SUGAR_CANE) || source.is(Items.REDSTONE)) {
            if (!moveItemStackTo(source, BrewingBarrelBlockEntity.SLOT_HOPS, BrewingBarrelBlockEntity.SLOT_HOPS + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(Items.WHEAT) || source.is(Items.GLOWSTONE_DUST)) {
            if (!moveItemStackTo(source, BrewingBarrelBlockEntity.SLOT_WHEAT, BrewingBarrelBlockEntity.SLOT_WHEAT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.is(Items.WATER_BUCKET)) {
            if (!moveItemStackTo(source, BrewingBarrelBlockEntity.SLOT_WATER, BrewingBarrelBlockEntity.SLOT_WATER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 31) {
            if (!moveItemStackTo(source, 31, 40, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(source, 4, 31, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, source);
        return quickMoved;
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

    private static BrewingBarrelBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof BrewingBarrelBlockEntity barrel) {
            return barrel;
        }
        throw new IllegalStateException("Expected BrewingBarrelBlockEntity at provided BlockPos");
    }
}
