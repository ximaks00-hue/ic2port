package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.item.ScrapItem;
import dev.ic2port.menu.MassFabricatorMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MassFabricatorBlockEntity extends BaseMachineBlockEntity {

    public static final double ENERGY_CAPACITY = 1_000_000.0D;
    public static final int TIER = EnergyTier.HV;
    public static final double ENERGY_DRAW_PER_TICK = EnergyTier.HV_MAX_PACKET;
    public static final double EU_PER_UU_MATTER = 1_000_000.0D;
    public static final double SCRAP_SPEED_MULTIPLIER = 6.0D;

    public static final int SLOT_SCRAP = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private double fabricationProgress;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> (int) Math.min(fabricationProgress, Integer.MAX_VALUE);
                case 1 -> (int) Math.min(EU_PER_UU_MATTER, Integer.MAX_VALUE);
                case 2 -> (int) Math.round(getStoredEnergy());
                case 3 -> (int) Math.round(getCapacity());
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MassFabricatorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.MASS_FABRICATOR_BE.get(), pos, state, SLOT_COUNT, ENERGY_CAPACITY);
    }

    @Override
    protected ItemStackHandler createItemHandler(final int totalSlots, final int processSlots) {
        return new ItemStackHandler(totalSlots) {
            @Override
            public boolean isItemValid(final int slot, final ItemStack stack) {
                if (slot >= processSlots) {
                    return stack.isEmpty() || stack.getItem() instanceof dev.ic2port.item.IUpgradeItem;
                }
                if (slot == SLOT_SCRAP) {
                    return stack.getItem() instanceof ScrapItem;
                }
                if (slot == SLOT_OUTPUT) {
                    return false;
                }
                return false;
            }

            @Override
            protected void onContentsChanged(final int slot) {
                if (slot >= processSlots) {
                    markUpgradeLayoutChanged();
                } else {
                    setChanged();
                }
            }
        };
    }

    @Override
    public int getTier() {
        return TIER;
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final MassFabricatorBlockEntity fabricator) {
        fabricator.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (consumeOverclockerLayoutReset()) {
            fabricationProgress = 0.0D;
        }

        ItemStack output = getItemHandler().getStackInSlot(SLOT_OUTPUT);
        if (output.getCount() >= output.getMaxStackSize()) {
            return;
        }

        double draw = Math.min(getStoredEnergy(), getScaledEnergyPerTick(ENERGY_DRAW_PER_TICK));
        if (draw <= 0.0D || !consumeEnergy(draw)) {
            return;
        }

        ItemStack scrap = getItemHandler().getStackInSlot(SLOT_SCRAP);
        boolean boosted = !scrap.isEmpty();
        if (boosted) {
            scrap.shrink(1);
        }
        fabricationProgress += draw * (boosted ? SCRAP_SPEED_MULTIPLIER : 1.0D);

        while (fabricationProgress >= EU_PER_UU_MATTER && canAddUuMatter(output)) {
            fabricationProgress -= EU_PER_UU_MATTER;

            if (output.isEmpty()) {
                output = new ItemStack(ItemRegistry.UU_MATTER.get());
                getItemHandler().setStackInSlot(SLOT_OUTPUT, output);
            } else {
                output.grow(1);
            }

            if (output.getCount() >= output.getMaxStackSize()) {
                break;
            }
        }

        setChanged();
    }

    private boolean canAddUuMatter(final ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ItemRegistry.UU_MATTER.get()) && output.getCount() < output.getMaxStackSize();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("FabricationProgress", fabricationProgress);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        fabricationProgress = Math.max(0.0D, tag.getDouble("FabricationProgress"));
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean isFabricating() {
        return fabricationProgress > 0.0D && getStoredEnergy() > 0.0D;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.mass_fabricator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new MassFabricatorMenu(containerId, playerInventory, this, data);
    }
}
