package dev.ic2port.blockentity;



import dev.ic2port.api.energy.EnergyTier;

import dev.ic2port.api.energy.IEnergyAcceptor;

import dev.ic2port.api.energy.IEnergyNode;

import dev.ic2port.menu.CropmatronMenu;

import dev.ic2port.setup.BlockEntityRegistry;

import dev.ic2port.setup.ItemRegistry;

import dev.ic2port.setup.ModCapabilities;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.util.CropMatronHelper;

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



/**

 * MV cropmatron — tends crops in a 9×5×9 area (hydration, fertilizer, weed removal).

 */

public class CropmatronBlockEntity extends BlockEntity implements IEnergyAcceptor, MenuProvider {



    public static final double ENERGY_CAPACITY = 16_000.0D;

    public static final double ENERGY_PER_CYCLE = 16.0D;

    public static final int TIER = EnergyTier.MV;

    public static final int TICK_INTERVAL = 40;

    public static final int FERTILIZER_BOOST = 24;



    private final ItemStackHandler inputHandler = new ItemStackHandler(1) {

        @Override

        public boolean isItemValid(final int slot, final ItemStack stack) {

            return stack.is(ItemRegistry.FERTILIZER.get())

                    || stack.is(ItemRegistry.HYDRATION_CELL.get())

                    || stack.is(ItemRegistry.WEED_EX.get());

        }



        @Override

        protected void onContentsChanged(final int slot) {

            setChanged();

        }

    };

    private final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> inputHandler);

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

            ContainerDataHelper.ignoreClientWrite();

        }



        @Override

        public int getCount() {

            return 2;

        }

    };



    private double storedEnergy;

    private int tickCounter;



    public CropmatronBlockEntity(final BlockPos pos, final BlockState state) {

        super(BlockEntityRegistry.CROPMATRON_BE.get(), pos, state);

    }



    public static void serverTick(

            final Level level,

            final BlockPos pos,

            final BlockState state,

            final CropmatronBlockEntity cropmatron) {

        cropmatron.tickServer();

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



        ItemStack supply = inputHandler.getStackInSlot(0);

        boolean hasSupply = !supply.isEmpty();

        int weeds = CropMatronHelper.clearWeeds(level, worldPosition);

        int hydrated = CropMatronHelper.hydrateFarmland(level, worldPosition);

        int tended = hasSupply

                ? CropMatronHelper.applySupply(level, worldPosition, supply, FERTILIZER_BOOST)

                : 0;



        if (weeds == 0 && hydrated == 0 && tended == 0) {

            return;

        }



        storedEnergy -= ENERGY_PER_CYCLE;

        if (tended > 0 && hasSupply) {

            if (supply.is(ItemRegistry.HYDRATION_CELL.get())) {

                int damage = supply.getDamageValue() + 1;

                if (damage >= supply.getMaxDamage()) {

                    inputHandler.setStackInSlot(0, ItemStack.EMPTY);

                } else {

                    supply.setDamageValue(damage);

                }

            } else {

                inputHandler.extractItem(0, 1, false);

            }

        }

        setChanged();

    }



    public IItemHandler getInputHandler() {

        return inputHandler;

    }



    public ContainerData getContainerData() {

        return data;

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

        return Component.translatable("block.ic2port.cropmatron");

    }



    @Nullable

    @Override

    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {

        return new CropmatronMenu(containerId, playerInventory, this, data);

    }



    @Override

    protected void saveAdditional(final CompoundTag tag) {

        super.saveAdditional(tag);

        tag.putDouble("StoredEnergy", storedEnergy);

        tag.put("Input", inputHandler.serializeNBT());

    }



    @Override

    public void load(final CompoundTag tag) {

        super.load(tag);

        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);

        if (tag.contains("Input")) {

            inputHandler.deserializeNBT(tag.getCompound("Input"));

        }

    }



    @Override

    public @NotNull <T> LazyOptional<T> getCapability(

            final @NotNull Capability<T> capability,

            final @Nullable Direction side) {

        if (capability == ForgeCapabilities.ITEM_HANDLER && side != Direction.DOWN) {

            return inputOptional.cast();

        }

        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {

            return energyOptional.cast();

        }

        return super.getCapability(capability, side);

    }



    @Override

    public void invalidateCaps() {

        super.invalidateCaps();

        inputOptional.invalidate();

        energyOptional.invalidate();

    }

}

