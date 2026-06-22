package dev.ic2port.blockentity;



import dev.ic2port.api.energy.EnergyTier;

import dev.ic2port.api.energy.IEnergyAcceptor;

import dev.ic2port.api.energy.IEnergyNode;

import dev.ic2port.blockentity.CropSticksBlockEntity;
import dev.ic2port.menu.CropHarvesterMenu;

import dev.ic2port.setup.BlockEntityRegistry;

import dev.ic2port.setup.ModCapabilities;

import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;

import net.minecraft.world.MenuProvider;

import net.minecraft.world.entity.player.Inventory;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraft.world.inventory.ContainerData;

import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;

import net.minecraftforge.common.capabilities.ForgeCapabilities;

import net.minecraftforge.common.util.LazyOptional;

import net.minecraftforge.items.IItemHandler;

import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;



import java.util.List;



/**

 * LV crop harvester — scans a 3×3×3 area and auto-harvests mature crops into its output buffer.

 */

public class CropHarvesterBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider {



    public static final double ENERGY_CAPACITY = 4_000.0D;

    public static final double ENERGY_PER_CYCLE = 5.0D;

    public static final int TIER = EnergyTier.LV;

    public static final int SCAN_RADIUS = 1;

    public static final int OUTPUT_SLOTS = 9;

    public static final int TICK_INTERVAL = 20;



    private final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS);

    private final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> outputHandler);

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);



    private final ContainerData data = new ContainerData() {

        @Override

        public int get(final int index) {

            return switch (index) {

                case 0 -> (int) Math.min(storedEnergy, Integer.MAX_VALUE);

                case 1 -> (int) Math.min(ENERGY_CAPACITY, Integer.MAX_VALUE);

                default -> 0;

            };

        }



        @Override

        public void set(final int index, final int value) {

            if (index == 0) {

                storedEnergy = value;

            }

        }



        @Override

        public int getCount() {

            return 2;

        }

    };



    private double storedEnergy;

    private int tickCounter;



    public CropHarvesterBlockEntity(final BlockPos pos, final BlockState state) {

        super(BlockEntityRegistry.CROP_HARVESTER_BE.get(), pos, state);

    }



    public static void serverTick(

            final Level level,

            final BlockPos pos,

            final BlockState state,

            final CropHarvesterBlockEntity harvester) {

        harvester.tickServer();

    }



    private void tickServer() {

        if (level == null || level.isClientSide) {

            return;

        }

        if (++tickCounter < TICK_INTERVAL) {

            return;

        }

        tickCounter = 0;

        if (storedEnergy < ENERGY_PER_CYCLE) {

            return;

        }



        boolean harvested = false;

        BlockPos center = worldPosition;

        for (BlockPos offset : BlockPos.betweenClosed(

                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),

                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {

            if (offset.equals(center)) {

                continue;

            }

            BlockEntity blockEntity = level.getBlockEntity(offset);

            if (!(blockEntity instanceof CropSticksBlockEntity crop)) {

                continue;

            }

            List<ItemStack> drops = crop.collectAutoHarvest();

            if (drops.isEmpty()) {

                continue;

            }

            harvested = true;

            for (ItemStack stack : drops) {

                insertOutput(stack);

            }

        }



        if (harvested) {

            storedEnergy -= ENERGY_PER_CYCLE;

            setChanged();

        }

    }



    public IItemHandler getOutputHandler() {

        return outputHandler;

    }



    public ContainerData getContainerData() {

        return data;

    }



    private void insertOutput(ItemStack stack) {

        if (stack.isEmpty()) {

            return;

        }

        for (int slot = 0; slot < outputHandler.getSlots(); slot++) {

            stack = outputHandler.insertItem(slot, stack, false);

            if (stack.isEmpty()) {

                return;

            }

        }

        if (level != null) {

            net.minecraft.world.level.block.Block.popResource(level, worldPosition, stack);

        }

    }



    @Override

    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {

        if (level == null || level.isClientSide || amount <= 0.0D) {

            return amount;

        }

        if (tier > TIER) {

            return amount;

        }

        double space = ENERGY_CAPACITY - storedEnergy;

        double accepted = Math.min(amount, space);

        if (accepted <= 0.0D) {

            return amount;

        }

        storedEnergy += accepted;

        setChanged();

        return amount - accepted;

    }



    @Override

    public double getCapacity() {

        return ENERGY_CAPACITY;

    }



    @Override

    public double getStoredEnergy() {

        return storedEnergy;

    }



    @Override

    public int getTier() {

        return TIER;

    }



    @Override

    public Component getDisplayName() {

        return Component.translatable("block.ic2port.crop_harvester");

    }



    @Nullable

    @Override

    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {

        return new CropHarvesterMenu(containerId, playerInventory, this, data);

    }



    @Override

    protected void saveAdditional(final CompoundTag tag) {

        super.saveAdditional(tag);

        tag.putDouble("StoredEnergy", storedEnergy);

        tag.put("Output", outputHandler.serializeNBT());

    }



    @Override

    public void load(final CompoundTag tag) {

        super.load(tag);

        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);

        if (tag.contains("Output")) {

            outputHandler.deserializeNBT(tag.getCompound("Output"));

        }

    }



    @Override

    public @NotNull <T> LazyOptional<T> getCapability(

            final @NotNull Capability<T> capability,

            final @Nullable Direction side) {

        if (capability == ForgeCapabilities.ITEM_HANDLER) {

            return outputOptional.cast();

        }

        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {

            return energyOptional.cast();

        }

        return super.getCapability(capability, side);

    }



    @Override

    public void invalidateCaps() {

        super.invalidateCaps();

        outputOptional.invalidate();

        energyOptional.invalidate();

    }

}

