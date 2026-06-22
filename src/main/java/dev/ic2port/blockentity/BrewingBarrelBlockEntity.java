package dev.ic2port.blockentity;

import dev.ic2port.util.ContainerDataHelper;
import dev.ic2port.brewing.BrewType;
import dev.ic2port.menu.BrewingBarrelMenu;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ItemRegistry;
import dev.ic2port.util.BeerHelper;
import dev.ic2port.util.PotionHelper;
import dev.ic2port.util.RumHelper;
import dev.ic2port.util.WhiskyHelper;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.Containers;
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
 * Ferments beer, rum, whisky, or potions over many ticks.
 */
public class BrewingBarrelBlockEntity extends BlockEntity implements MenuProvider {

    public static final int BEER_DURATION = 6000;
    public static final int SLOT_HOPS = 0;
    public static final int SLOT_WHEAT = 1;
    public static final int SLOT_WATER = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(final int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(final int slot, final ItemStack stack) {
            return switch (slot) {
                case SLOT_HOPS -> stack.is(ItemRegistry.HOPS.get())
                        || stack.is(Items.SUGAR_CANE)
                        || stack.is(Items.REDSTONE);
                case SLOT_WHEAT -> stack.is(Items.WHEAT) || stack.is(Items.GLOWSTONE_DUST);
                case SLOT_WATER -> stack.is(Items.WATER_BUCKET);
                case SLOT_OUTPUT -> false;
                default -> false;
            };
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(final int index) {
            return switch (index) {
                case 0 -> brewProgress;
                case 1 -> brewDurationMax;
                case 2 -> brewing ? 1 : 0;
                case 3 -> temperature;
                case 4 -> activeBrewType.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(final int index, final int value) {
            ContainerDataHelper.ignoreClientWrite();
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private int brewProgress;
    private int brewDurationMax = BEER_DURATION;
    private boolean brewing;
    private int temperature = 20;
    private BrewType activeBrewType = BrewType.NONE;
    private int batchHops;
    private int batchWheat;
    private int batchSugarCane;
    private int batchRedstone;
    private int batchGlowstone;

    private final LazyOptional<IItemHandler> itemHandlerOptional = LazyOptional.of(this::getItemHandlerCapability);

    public BrewingBarrelBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.BREWING_BARREL_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final BrewingBarrelBlockEntity barrel) {
        barrel.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (brewing) {
            brewProgress++;
            temperature = 22 + (int) (8 * Math.sin(brewProgress / 80.0D));
            if (brewProgress >= brewDurationMax) {
                finishBrew();
            }
        } else {
            temperature = Math.max(20, temperature - 1);
            tryStartBrew();
        }
    }

    private void tryStartBrew() {
        BrewType detected = detectBrewType();
        if (detected == BrewType.NONE) {
            return;
        }
        switch (detected) {
            case BEER -> tryStartBeer();
            case RUM -> tryStartRum();
            case WHISKY -> tryStartWhisky();
            case POTION -> tryStartPotion();
            default -> {
            }
        }
    }

    private BrewType detectBrewType() {
        ItemStack hops = itemHandler.getStackInSlot(SLOT_HOPS);
        ItemStack wheat = itemHandler.getStackInSlot(SLOT_WHEAT);
        ItemStack water = itemHandler.getStackInSlot(SLOT_WATER);

        if (hops.is(Items.SUGAR_CANE) && wheat.isEmpty() && water.isEmpty()) {
            return BrewType.RUM;
        }
        if (hops.isEmpty() && wheat.is(Items.WHEAT) && water.isEmpty()) {
            return BrewType.WHISKY;
        }
        if (hops.is(Items.REDSTONE) && wheat.is(Items.GLOWSTONE_DUST) && water.isEmpty()) {
            return BrewType.POTION;
        }
        if (hops.is(ItemRegistry.HOPS.get()) && wheat.is(Items.WHEAT) && water.is(Items.WATER_BUCKET)) {
            return BrewType.BEER;
        }
        return BrewType.NONE;
    }

    private void tryStartBeer() {
        if (!canOutput(ItemRegistry.BEER.get())) {
            return;
        }
        ItemStack hops = itemHandler.getStackInSlot(SLOT_HOPS);
        ItemStack wheat = itemHandler.getStackInSlot(SLOT_WHEAT);
        ItemStack water = itemHandler.getStackInSlot(SLOT_WATER);
        if (hops.getCount() < BeerHelper.HOPS_COST
                || wheat.getCount() < BeerHelper.WHEAT_COST
                || !water.is(Items.WATER_BUCKET)) {
            return;
        }
        batchHops = BeerHelper.HOPS_COST;
        batchWheat = BeerHelper.WHEAT_COST;
        batchSugarCane = 0;
        hops.shrink(BeerHelper.HOPS_COST);
        wheat.shrink(BeerHelper.WHEAT_COST);
        itemHandler.setStackInSlot(SLOT_HOPS, hops);
        itemHandler.setStackInSlot(SLOT_WHEAT, wheat);
        itemHandler.setStackInSlot(SLOT_WATER, new ItemStack(Items.BUCKET));
        startBrew(BrewType.BEER, BEER_DURATION);
    }

    private void tryStartRum() {
        if (!canOutput(ItemRegistry.RUM.get())) {
            return;
        }
        ItemStack cane = itemHandler.getStackInSlot(SLOT_HOPS);
        if (cane.getCount() < RumHelper.SUGAR_CANE_COST) {
            return;
        }
        batchSugarCane = RumHelper.SUGAR_CANE_COST;
        batchHops = 0;
        batchWheat = 0;
        cane.shrink(RumHelper.SUGAR_CANE_COST);
        itemHandler.setStackInSlot(SLOT_HOPS, cane);
        startBrew(BrewType.RUM, RumHelper.BREW_DURATION);
    }

    private void tryStartWhisky() {
        if (!canOutput(ItemRegistry.WHISKY.get())) {
            return;
        }
        ItemStack wheat = itemHandler.getStackInSlot(SLOT_WHEAT);
        if (wheat.getCount() < WhiskyHelper.GRIST_COST) {
            return;
        }
        batchHops = 0;
        batchWheat = WhiskyHelper.GRIST_COST;
        batchSugarCane = 0;
        wheat.shrink(WhiskyHelper.GRIST_COST);
        itemHandler.setStackInSlot(SLOT_WHEAT, wheat);
        startBrew(BrewType.WHISKY, WhiskyHelper.BREW_DURATION);
    }

    private void tryStartPotion() {
        if (!canOutput(ItemRegistry.BREWED_POTION.get())) {
            return;
        }
        ItemStack redstone = itemHandler.getStackInSlot(SLOT_HOPS);
        ItemStack glowstone = itemHandler.getStackInSlot(SLOT_WHEAT);
        if (redstone.getCount() < PotionHelper.REDSTONE_COST
                || glowstone.getCount() < PotionHelper.GLOWSTONE_COST) {
            return;
        }
        batchRedstone = PotionHelper.REDSTONE_COST;
        batchGlowstone = PotionHelper.GLOWSTONE_COST;
        batchHops = 0;
        batchWheat = 0;
        batchSugarCane = 0;
        redstone.shrink(PotionHelper.REDSTONE_COST);
        glowstone.shrink(PotionHelper.GLOWSTONE_COST);
        itemHandler.setStackInSlot(SLOT_HOPS, redstone);
        itemHandler.setStackInSlot(SLOT_WHEAT, glowstone);
        startBrew(BrewType.POTION, PotionHelper.BREW_DURATION);
    }

    private void startBrew(final BrewType type, final int duration) {
        activeBrewType = type;
        brewDurationMax = duration;
        brewing = true;
        brewProgress = 0;
        setChanged();
    }

    private boolean canOutput(final net.minecraft.world.item.Item product) {
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty() || (output.is(product) && output.getCount() < output.getMaxStackSize());
    }

    private void finishBrew() {
        BrewType finished = activeBrewType;
        int progress = brewProgress;
        brewing = false;
        brewProgress = 0;
        activeBrewType = BrewType.NONE;

        ItemStack product = switch (finished) {
            case BEER -> BeerHelper.createBeer(batchHops, batchWheat, 0);
            case RUM -> RumHelper.createRum(batchSugarCane);
            case WHISKY -> WhiskyHelper.createWhisky(WhiskyHelper.yearsFromProgress(progress));
            case POTION -> PotionHelper.createPotion(batchRedstone, batchGlowstone);
            default -> ItemStack.EMPTY;
        };
        if (product.isEmpty()) {
            setChanged();
            return;
        }
        mergeOutput(product);
        setChanged();
    }

    private void mergeOutput(final ItemStack product) {
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, product);
            return;
        }
        if (!ItemStack.isSameItemSameTags(output, product)) {
            dropOverflow(product);
            return;
        }
        int transferable = Math.min(product.getCount(), output.getMaxStackSize() - output.getCount());
        if (transferable > 0) {
            output.grow(transferable);
            itemHandler.setStackInSlot(SLOT_OUTPUT, output);
        }
        int remainder = product.getCount() - transferable;
        if (remainder > 0) {
            ItemStack leftover = product.copy();
            leftover.setCount(remainder);
            dropOverflow(leftover);
        }
    }

    private void dropOverflow(final ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return;
        }
        Containers.dropItemStack(
                level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                stack);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isBrewing() {
        return brewing;
    }

    public BrewType getActiveBrewType() {
        return activeBrewType;
    }

    public int getTemperature() {
        return temperature;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ic2port.brewing_barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int containerId, final Inventory playerInventory, final Player player) {
        return new BrewingBarrelMenu(containerId, playerInventory, this, data);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", itemHandler.serializeNBT());
        tag.putInt("BrewProgress", brewProgress);
        tag.putInt("BrewDurationMax", brewDurationMax);
        tag.putBoolean("Brewing", brewing);
        tag.putInt("Temperature", temperature);
        tag.putInt("BrewType", activeBrewType.ordinal());
        tag.putInt("BatchHops", batchHops);
        tag.putInt("BatchWheat", batchWheat);
        tag.putInt("BatchSugarCane", batchSugarCane);
        tag.putInt("BatchRedstone", batchRedstone);
        tag.putInt("BatchGlowstone", batchGlowstone);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Items"));
        brewProgress = tag.getInt("BrewProgress");
        brewDurationMax = tag.getInt("BrewDurationMax");
        if (brewDurationMax <= 0) {
            brewDurationMax = BEER_DURATION;
        }
        brewing = tag.getBoolean("Brewing");
        temperature = tag.getInt("Temperature");
        activeBrewType = BrewType.fromIndex(tag.getInt("BrewType"));
        batchHops = tag.getInt("BatchHops");
        batchWheat = tag.getInt("BatchWheat");
        batchSugarCane = tag.getInt("BatchSugarCane");
        batchRedstone = tag.getInt("BatchRedstone");
        batchGlowstone = tag.getInt("BatchGlowstone");
    }

    public IItemHandler getItemHandlerCapability() {
        return itemHandler;
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
