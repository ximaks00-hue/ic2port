package dev.ic2port.menu;

import dev.ic2port.blockentity.GeothermalGeneratorBlockEntity;
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
 * Server-client container for the geothermal generator.
 */
public class GeothermalGeneratorMenu extends AbstractContainerMenu {

    private static final int PLAYER_INVENTORY_START_Y = 84;
    private static final int HOTBAR_Y = 142;

    private static final int SLOT_INPUT_X = 56;
    private static final int SLOT_INPUT_Y = 17;
    private static final int SLOT_OUTPUT_X = 56;
    private static final int SLOT_OUTPUT_Y = 53;

    private static final int DATA_STORED_ENERGY = 0;
    private static final int DATA_MAX_ENERGY = 1;
    private static final int DATA_FLUID_AMOUNT = 2;
    private static final int DATA_FLUID_CAPACITY = 3;

    private final GeothermalGeneratorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public GeothermalGeneratorMenu(final int containerId, final Inventory playerInventory, final FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(4));
    }

    public GeothermalGeneratorMenu(
            final int containerId,
            final Inventory playerInventory,
            final GeothermalGeneratorBlockEntity blockEntity,
            final ContainerData data) {
        super(MenuTypeRegistry.GEOTHERMAL_GENERATOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.level = playerInventory.player.level();
        this.data = data;

        IItemHandler itemHandler = blockEntity.getFullItemHandler();

        this.addSlot(new SlotItemHandler(itemHandler, GeothermalGeneratorBlockEntity.SLOT_INPUT, SLOT_INPUT_X, SLOT_INPUT_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.is(Items.LAVA_BUCKET);
            }
        });
        this.addSlot(new SlotItemHandler(
                itemHandler,
                GeothermalGeneratorBlockEntity.SLOT_OUTPUT,
                SLOT_OUTPUT_X,
                SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(final ItemStack stack) {
                return stack.isEmpty() || stack.is(Items.BUCKET);
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    public int getEnergyScaled(final int height) {
        int energy = data.get(DATA_STORED_ENERGY);
        int maxEnergy = data.get(DATA_MAX_ENERGY);
        if (maxEnergy <= 0 || energy <= 0) {
            return 0;
        }
        return energy * height / maxEnergy;
    }

    public int getFluidScaled(final int height) {
        int fluid = data.get(DATA_FLUID_AMOUNT);
        int maxFluid = data.get(DATA_FLUID_CAPACITY);
        if (maxFluid <= 0 || fluid <= 0) {
            return 0;
        }
        return fluid * height / maxFluid;
    }

    public int getFluidAmount() {
        return data.get(DATA_FLUID_AMOUNT);
    }

    public int getFluidCapacity() {
        return data.get(DATA_FLUID_CAPACITY);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        var slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = slot.getItem();
        quickMoved = sourceStack.copy();

        if (index < 2) {
            if (!moveItemStackTo(sourceStack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.LAVA_BUCKET)) {
            if (!moveItemStackTo(
                    sourceStack,
                    GeothermalGeneratorBlockEntity.SLOT_INPUT,
                    GeothermalGeneratorBlockEntity.SLOT_INPUT + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (sourceStack.is(Items.BUCKET)) {
            if (!moveItemStackTo(
                    sourceStack,
                    GeothermalGeneratorBlockEntity.SLOT_OUTPUT,
                    GeothermalGeneratorBlockEntity.SLOT_OUTPUT + 1,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 29) {
            if (!moveItemStackTo(sourceStack, 29, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, 2, 29, false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (sourceStack.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, sourceStack);
        return quickMoved;
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
                        PLAYER_INVENTORY_START_Y + row * 18
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
                    HOTBAR_Y
            ));
        }
    }

    private static GeothermalGeneratorBlockEntity getBlockEntity(
            final Inventory playerInventory,
            final FriendlyByteBuf extraData) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof GeothermalGeneratorBlockEntity generator) {
            return generator;
        }
        throw new IllegalStateException("Expected GeothermalGeneratorBlockEntity at provided BlockPos");
    }
}
