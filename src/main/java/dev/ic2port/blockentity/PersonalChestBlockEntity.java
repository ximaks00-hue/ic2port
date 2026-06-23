package dev.ic2port.blockentity;

import dev.ic2port.api.tiles.IPersonalStorage;
import dev.ic2port.menu.PersonalChestMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.FullInventoryAccess;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Owner-bound 54-slot chest with friends ACL.
 */
public class PersonalChestBlockEntity extends AbstractPersonalStorageBlockEntity
        implements MenuProvider, FullInventoryAccess, IPersonalStorage {

    public static final int SLOT_COUNT = 54;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(() -> itemHandler);

    public PersonalChestBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.PERSONAL_CHEST_BE.get(), pos, state);
    }

    @Override
    public ItemStackHandler getFullItemHandler() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.personal_chest");
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
                return index == 0 ? getFriends().size() : 0;
            }

            @Override
            public void set(final int index, final int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
        return new PersonalChestMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        savePersonalData(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        loadPersonalData(tag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOptional.invalidate();
    }
}
