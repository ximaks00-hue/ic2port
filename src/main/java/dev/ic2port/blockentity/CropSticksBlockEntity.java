package dev.ic2port.blockentity;

import dev.ic2port.api.crops.ICrop;
import dev.ic2port.api.crops.ICropTile;
import dev.ic2port.block.CropSticksBlock;
import dev.ic2port.crop.BaseSeedEntry;
import dev.ic2port.crop.CropRegistry;
import dev.ic2port.item.HydrationCellItem;
import dev.ic2port.item.WeedExItem;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.util.CropAnalyzerHelper;
import dev.ic2port.util.CropBreedingHelper;
import dev.ic2port.util.CropHelper;
import dev.ic2port.util.CropSeedHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a single IC2 crop planted on crop sticks.
 */
public class CropSticksBlockEntity extends BlockEntity implements ICropTile {

    private static final int WEED_CHANCE_EMPTY = 250;

    @Nullable
    private ICrop crop;
    private int growthStage;
    private int growthPoints;
    private int growthStat = 1;
    private int gainStat = 1;
    private int resistanceStat = 1;
    private int scanLevel;
    private int hydrationStorage;
    private int weedExStorage;

    public CropSticksBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CROP_STICKS_BE.get(), pos, state);
    }

    public void onRandomTick(final RandomSource random) {
        if (level == null || level.isClientSide) {
            return;
        }
        if (crop == null) {
            decayEnvironmentStorage(random);
            CropBreedingHelper.tryBreedEmptyCenter(this, random);
            if (crop == null && weedExStorage <= 0 && random.nextInt(WEED_CHANCE_EMPTY) == 0) {
                plant(CropRegistry.WEED, 1, 1, 1, 1, 0);
            }
            return;
        }

        decayEnvironmentStorage(random);

        if (weedExStorage <= 0) {
            if (CropHelper.canSpreadWeed(this)) {
                spreadWeed(random);
            } else if (growthStat >= 24 && crop != CropRegistry.WEED && random.nextInt(200) == 0) {
                convertToWeed();
            }
        }

        trySubsoilMutation(random);

        if (!crop.canProgressGrowth(this)) {
            return;
        }

        if (!crop.canGrow(this)) {
            return;
        }
        growthPoints += calculateGrowthSpeed();
        if (growthPoints >= crop.getGrowthDuration(this)) {
            growthPoints = 0;
            growthStage++;
            syncVisualStage();
            setChanged();
        }
    }

    public int calculateGrowthSpeed() {
        int speed = 1;
        if (getLightLevel() >= 9) {
            speed++;
        }
        if (getHumidity() >= 12) {
            speed++;
        }
        if (getNutrients() >= 10) {
            speed++;
        }
        speed += growthStat / 8;
        float multiplier = CropHelper.getSubsoilGrowthMultiplier(this);
        return Math.max(1, Math.round(speed * multiplier));
    }

    private void trySubsoilMutation(final RandomSource random) {
        if (crop == null || level == null) {
            return;
        }
        if (crop == CropRegistry.NETHER_WART && CropHelper.hasSnowSubsoil(this) && random.nextInt(400) == 0) {
            mutateCrop(CropRegistry.TERRA_WART);
        } else if (crop == CropRegistry.TERRA_WART && CropHelper.hasSoulSandSubsoil(this) && random.nextInt(400) == 0) {
            mutateCrop(CropRegistry.NETHER_WART);
        }
    }

    private void mutateCrop(final ICrop newCrop) {
        crop = newCrop;
        growthPoints = 0;
        syncVisualStage();
        setChanged();
    }

    public boolean tryPlant(final Player player, final ItemStack stack) {
        BaseSeedEntry seed = CropRegistry.getBaseSeed(stack);
        if (seed == null) {
            return false;
        }
        if (crop != null) {
            return false;
        }
        if (!plant(seed.crop(), seed.stage(), seed.growth(), seed.gain(), seed.resistance(), scanLevel)) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(seed.stackSize());
        }
        return true;
    }

    public boolean tryFertilize() {
        return tryFertilize(32);
    }

    private void decayEnvironmentStorage(final RandomSource random) {
        if (hydrationStorage > 0 && random.nextInt(4) == 0) {
            hydrationStorage--;
            setChanged();
        }
        if (weedExStorage > 0 && random.nextInt(8) == 0) {
            weedExStorage--;
            setChanged();
        }
    }

    public boolean tryApplyHydration(final int amount) {
        if (crop == null || hydrationStorage >= HydrationCellItem.MAX_STORAGE) {
            return false;
        }
        hydrationStorage = Math.min(HydrationCellItem.MAX_STORAGE, hydrationStorage + amount);
        setChanged();
        return true;
    }

    public boolean tryApplyWeedEx(final int amount) {
        if (crop == null || weedExStorage >= WeedExItem.MAX_STORAGE) {
            return false;
        }
        if (weedExStorage > 0) {
            gainStat = Math.max(1, gainStat - 1);
        }
        weedExStorage = Math.min(WeedExItem.MAX_STORAGE, weedExStorage + amount);
        setChanged();
        return true;
    }

    public boolean tryFertilize(final int boost) {
        if (crop == null || !crop.canGrow(this)) {
            return false;
        }
        growthPoints += boost;
        while (growthPoints >= crop.getGrowthDuration(this) && crop.canGrow(this)) {
            growthPoints -= crop.getGrowthDuration(this);
            growthStage++;
        }
        syncVisualStage();
        setChanged();
        return true;
    }

    public boolean tryHarvest(final Player player) {
        if (crop == null || !crop.canBeHarvested(this)) {
            return false;
        }
        dropStacks(collectHarvestDrops(false), player);
        completeHarvest(false, player);
        return true;
    }

    /**
     * @return harvested stacks for automated collectors (reduced yield)
     */
    public List<ItemStack> collectAutoHarvest() {
        if (crop == null || !crop.canBeHarvested(this)) {
            return List.of();
        }
        List<ItemStack> drops = collectHarvestDrops(true);
        ItemStack seeds = rollSeedDrop(true);
        if (!seeds.isEmpty()) {
            drops.add(seeds);
        }
        applyAfterHarvestState();
        return drops;
    }

    public boolean tryScan(final net.minecraft.world.entity.player.Player player) {
        if (crop == null) {
            return false;
        }
        if (scanLevel < 4) {
            scanLevel++;
            setChanged();
        }
        CropAnalyzerHelper.displayScan(this, player);
        return true;
    }

    private List<ItemStack> collectHarvestDrops(final boolean automated) {
        List<ItemStack> drops = new ArrayList<>();
        if (crop == null) {
            return drops;
        }
        for (ItemStack drop : crop.getDrops(this)) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack stack = drop.copy();
            if (automated) {
                stack.setCount(Math.max(stack.getCount() > 1 ? stack.getCount() / 2 : stack.getCount(), 1));
            }
            drops.add(stack);
        }
        return drops;
    }

    private void completeHarvest(final boolean automated, final @Nullable Player player) {
        ItemStack seeds = rollSeedDrop(automated);
        if (!seeds.isEmpty()) {
            dropStack(seeds, player);
        }
        applyAfterHarvestState();
    }

    private ItemStack rollSeedDrop(final boolean automated) {
        if (crop == null || level == null) {
            return ItemStack.EMPTY;
        }
        float seedChance = crop.getSeedDropChance(this);
        if (automated) {
            seedChance *= 0.5F;
        }
        if (level.random.nextFloat() <= seedChance) {
            return crop.getSeeds(this);
        }
        return ItemStack.EMPTY;
    }

    private void applyAfterHarvestState() {
        if (crop == null) {
            return;
        }
        growthStage = crop.getAfterHarvestStage(this);
        growthPoints = 0;
        syncVisualStage();
        setChanged();
    }

    public boolean tryPick(final Player player) {
        if (crop == null) {
            return false;
        }
        dropStack(crop.getSeeds(this), player);
        clearCrop();
        return true;
    }

    public void dropContents() {
        if (level == null || crop == null) {
            return;
        }
        dropStacks(collectHarvestDrops(false), null);
        dropStack(crop.getSeeds(this), null);
    }

    private void dropStacks(final List<ItemStack> stacks, final @Nullable Player player) {
        if (level == null) {
            return;
        }
        for (ItemStack stack : stacks) {
            dropStack(stack, player);
        }
    }

    private void dropStack(final ItemStack stack, final @Nullable Player player) {
        if (level == null || stack.isEmpty()) {
            return;
        }
        if (player != null && player.getInventory().add(stack.copy())) {
            return;
        }
        Block.popResource(level, worldPosition, stack.copy());
    }

    public void applyBreedingResult(
            final ICrop childCrop,
            final int growth,
            final int gain,
            final int resistance) {
        plant(childCrop, 1, growth, gain, resistance, 0);
    }

    public void convertToWeed() {
        if (level == null || crop == null) {
            return;
        }
        crop = CropRegistry.WEED;
        growthStage = 1;
        growthPoints = 0;
        syncVisualStage();
        setChanged();
    }

    public boolean tryClearWeed() {
        if (crop != CropRegistry.WEED) {
            return false;
        }
        clearCrop();
        return true;
    }

    private void spreadWeed(final RandomSource random) {
        if (level == null) {
            return;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            if (!(blockEntity instanceof CropSticksBlockEntity neighbor) || neighbor.getCrop() == null) {
                continue;
            }
            if (neighbor.getResistanceStat() >= 24 || CropHelper.isWeed(neighbor) || neighbor.getWeedExStorage() > 0) {
                continue;
            }
            if (random.nextInt(96) == 0) {
                neighbor.convertToWeed();
            }
        }
    }

    private boolean plant(
            final ICrop newCrop,
            final int stage,
            final int growth,
            final int gain,
            final int resistance,
            final int scan) {
        if (level == null || crop != null) {
            return false;
        }
        crop = newCrop;
        growthStage = stage;
        growthStat = growth;
        gainStat = gain;
        resistanceStat = resistance;
        scanLevel = scan;
        growthPoints = 0;
        syncVisualStage();
        setChanged();
        return true;
    }

    private void clearCrop() {
        crop = null;
        growthStage = 0;
        growthPoints = 0;
        syncVisualStage();
        setChanged();
    }

    private void syncVisualStage() {
        if (level == null || level.isClientSide) {
            return;
        }
        int visual = crop == null ? 0 : Math.min(CropSticksBlock.MAX_STAGE, growthStage);
        BlockState updated = getBlockState().setValue(CropSticksBlock.STAGE, visual);
        level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
    }

    @Override
    @Nullable
    public ICrop getCrop() {
        return crop;
    }

    @Override
    public void setCrop(final ICrop crop) {
        this.crop = crop;
    }

    @Override
    public int getGrowthStage() {
        return growthStage;
    }

    @Override
    public void setGrowthStage(final int stage) {
        this.growthStage = stage;
    }

    @Override
    public int getGrowthPoints() {
        return growthPoints;
    }

    @Override
    public void setGrowthPoints(final int points) {
        this.growthPoints = points;
    }

    @Override
    public int getScanLevel() {
        return scanLevel;
    }

    @Override
    public void setScanLevel(final int level) {
        this.scanLevel = Math.max(0, Math.min(4, level));
    }

    @Override
    public int getGainStat() {
        return gainStat;
    }

    @Override
    public int getGrowthStat() {
        return growthStat;
    }

    @Override
    public int getResistanceStat() {
        return resistanceStat;
    }

    @Override
    public void setGainStat(final int value) {
        this.gainStat = value;
    }

    @Override
    public void setGrowthStat(final int value) {
        this.growthStat = value;
    }

    @Override
    public void setResistanceStat(final int value) {
        this.resistanceStat = value;
    }

    @Override
    public int getLightLevel() {
        return level == null ? 0 : level.getMaxLocalRawBrightness(worldPosition);
    }

    @Override
    public int getHumidity() {
        if (level == null) {
            return 0;
        }
        BlockState below = level.getBlockState(worldPosition.below());
        if (below.is(Blocks.FARMLAND) && below.hasProperty(BlockStateProperties.MOISTURE)) {
            return (below.getValue(BlockStateProperties.MOISTURE) > 0 ? 16 : 8) + hydrationStorage / 5;
        }
        for (BlockPos check : BlockPos.betweenClosed(worldPosition.offset(-2, -1, -2), worldPosition.offset(2, -1, 2))) {
            if (level.getFluidState(check).isSource()) {
                return 18 + hydrationStorage / 5;
            }
        }
        return 6 + hydrationStorage / 5;
    }

    @Override
    public int getHydrationStorage() {
        return hydrationStorage;
    }

    @Override
    public void setHydrationStorage(final int value) {
        this.hydrationStorage = Math.max(0, Math.min(HydrationCellItem.MAX_STORAGE, value));
    }

    @Override
    public int getWeedExStorage() {
        return weedExStorage;
    }

    @Override
    public void setWeedExStorage(final int value) {
        this.weedExStorage = Math.max(0, Math.min(WeedExItem.MAX_STORAGE, value));
    }

    @Override
    public int getNutrients() {
        return level != null && level.getBlockState(worldPosition.below()).is(Blocks.FARMLAND) ? 14 : 4;
    }

    @Override
    public ItemStack createSeeds(
            final ICrop seedCrop,
            final int growth,
            final int gain,
            final int resistance,
            final int scan) {
        return CropSeedHelper.createSeed(seedCrop, growth, gain, resistance, scan);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        if (crop != null) {
            tag.putString("CropId", crop.id().toString());
        }
        tag.putInt("GrowthStage", growthStage);
        tag.putInt("GrowthPoints", growthPoints);
        tag.putByte("GrowthStat", (byte) growthStat);
        tag.putByte("GainStat", (byte) gainStat);
        tag.putByte("ResistanceStat", (byte) resistanceStat);
        tag.putByte("ScanLevel", (byte) scanLevel);
        tag.putShort("HydrationStorage", (short) hydrationStorage);
        tag.putShort("WeedExStorage", (short) weedExStorage);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CropId")) {
            crop = CropRegistry.get(new ResourceLocation(tag.getString("CropId")));
        } else {
            crop = null;
        }
        growthStage = tag.getInt("GrowthStage");
        growthPoints = tag.getInt("GrowthPoints");
        growthStat = tag.getByte("GrowthStat");
        gainStat = tag.getByte("GainStat");
        resistanceStat = tag.getByte("ResistanceStat");
        scanLevel = tag.getByte("ScanLevel");
        hydrationStorage = tag.getShort("HydrationStorage");
        weedExStorage = tag.getShort("WeedExStorage");
    }
}
