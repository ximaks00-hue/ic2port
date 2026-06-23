package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.menu.UuCropLibraryMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * UU-Matter crop library — stores crop seeds and clones them using UU-Matter and EU.
 */
public class UuCropLibraryBlockEntity extends BaseMachineBlockEntity implements MenuProvider {

    public static final int SLOT_COUNT = 9;
    public static final double UU_CAPACITY = 512.0D;
    public static final double STORE_ENERGY_COST = 512.0D;
    public static final double CLONE_EU_PER_STAT = 10_000.0D;
    public static final double CLONE_UU_COST = 1.0D;

    private final ItemStackHandler seedStorage = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return stack.is(ItemRegistry.CROP_SEED.get());
        }

        @Override
        public int getSlotLimit(final int slot) {
            return 1;
        }
    };

    private double storedUuMatter;

    public UuCropLibraryBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.UU_CROP_LIBRARY_BE.get(), pos, state, 0, 1_000_000.0D);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final UuCropLibraryBlockEntity library) {
        // Passive storage — operations are triggered from the GUI.
    }

    @Override
    public int getTier() {
        return EnergyTier.EV;
    }

    public ItemStackHandler getSeedStorage() {
        return seedStorage;
    }

    public double getStoredUuMatter() {
        return storedUuMatter;
    }

    public boolean storeSeed(final int slot, final ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return false;
        }
        if (!consumeEnergy(STORE_ENERGY_COST) || !seedStorage.getStackInSlot(slot).isEmpty()) {
            return false;
        }
        seedStorage.setStackInSlot(slot, stack.copyWithCount(1));
        return true;
    }

    public boolean cloneSeed(final int slot) {
        if (level == null || level.isClientSide) {
            return false;
        }
        ItemStack seed = seedStorage.getStackInSlot(slot);
        if (seed.isEmpty() || storedUuMatter < CLONE_UU_COST) {
            return false;
        }
        int stats = Math.max(1, seed.getOrCreateTag().getInt("Stats"));
        double euCost = stats * CLONE_EU_PER_STAT;
        if (!consumeEnergy(euCost)) {
            return false;
        }
        storedUuMatter -= CLONE_UU_COST;
        seed.grow(1);
        setChanged();
        return true;
    }

    public void insertUuMatter(final ItemStack stack) {
        if (!stack.isEmpty() && stack.is(ItemRegistry.UU_MATTER.get())) {
            int moved = (int) Math.min(stack.getCount(), UU_CAPACITY - storedUuMatter);
            if (moved > 0) {
                storedUuMatter += moved;
                stack.shrink(moved);
                setChanged();
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.uu_crop_library");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new UuCropLibraryMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Seeds", seedStorage.serializeNBT());
        tag.putDouble("StoredUu", storedUuMatter);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Seeds")) {
            seedStorage.deserializeNBT(tag.getCompound("Seeds"));
        }
        storedUuMatter = Math.min(tag.getDouble("StoredUu"), UU_CAPACITY);
    }
}
