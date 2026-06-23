package dev.ic2port.blockentity;

import dev.ic2port.api.tiles.IPersonalStorage;
import dev.ic2port.menu.PersonalTankMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.InsertOnlyFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Owner-bound fluid tank with friends ACL.
 */
public class PersonalTankBlockEntity extends AbstractPersonalStorageBlockEntity
        implements MenuProvider, IPersonalStorage {

    public static final int TANK_CAPACITY_MB = 16_000;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY_MB) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }

        @Override
        public int fill(final FluidStack resource, final FluidAction action) {
            if (!resource.isEmpty() && !getFluid().isEmpty() && !getFluid().isFluidEqual(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }
    };
    private final InsertOnlyFluidHandler externalTank = new InsertOnlyFluidHandler(tank);
    private final LazyOptional<IFluidHandler> tankOptional = LazyOptional.of(() -> externalTank);

    public PersonalTankBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.PERSONAL_TANK_BE.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.personal_tank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        if (!canAccess(player)) {
            return null;
        }
        bindOwner(player);
        net.minecraft.world.inventory.ContainerData data = new net.minecraft.world.inventory.ContainerData() {
            @Override
            public int get(final int index) {
                return switch (index) {
                    case 0 -> tank.getFluidAmount();
                    case 1 -> tank.getCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(final int index, final int value) {
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        return new PersonalTankMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Tank", tank.writeToNBT(new CompoundTag()));
        savePersonalData(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Tank")) {
            tank.readFromNBT(tag.getCompound("Tank"));
        }
        loadPersonalData(tag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return tankOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        tankOptional.invalidate();
    }
}
