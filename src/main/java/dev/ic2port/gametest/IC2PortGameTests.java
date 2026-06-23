package dev.ic2port.gametest;

import dev.ic2port.Reference;
import dev.ic2port.blockentity.TeleporterBlockEntity;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.BlockRegistry;
import dev.ic2port.setup.ModFluids;
import dev.ic2port.setup.RecipeTypeRegistry;
import dev.ic2port.util.TeleporterCostHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;

/**
 * Release-gate baseline GameTests for critical systems.
 */
@GameTestHolder(Reference.MOD_ID)
public final class IC2PortGameTests {

    private IC2PortGameTests() {
        throw new UnsupportedOperationException("Utility class");
    }

    @GameTest(template = "empty", batch = "ic2port.release_gate", timeoutTicks = 100)
    public static void euTransferChainBaseline(final GameTestHelper helper) {
        BlockPos generatorPos = new BlockPos(1, 2, 1);
        BlockPos cablePos = new BlockPos(2, 2, 1);
        BlockPos storagePos = new BlockPos(3, 2, 1);

        helper.setBlock(generatorPos, BlockRegistry.SOLID_FUEL_GENERATOR.get());
        helper.setBlock(cablePos, BlockRegistry.COPPER_CABLE.get());
        helper.setBlock(storagePos, BlockRegistry.BATBOX.get());

        helper.runAfterDelay(2, () -> {
            BlockEntity generator = helper.getBlockEntity(generatorPos);
            BlockEntity cable = helper.getBlockEntity(cablePos);
            BlockEntity storage = helper.getBlockEntity(storagePos);
            if (generator == null || cable == null || storage == null) {
                throw new GameTestAssertException("Expected generator/cable/storage block entities to exist");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "ic2port.release_gate", timeoutTicks = 100)
    public static void machineProcessingBaseline(final GameTestHelper helper) {
        RecipeManager recipes = helper.getLevel().getRecipeManager();

        if (recipes.getAllRecipesFor(RecipeTypeRegistry.MACERATOR.get()).isEmpty()) {
            throw new GameTestAssertException("No macerator recipes loaded");
        }
        if (recipes.getAllRecipesFor(RecipeTypeRegistry.COMPRESSOR.get()).isEmpty()) {
            throw new GameTestAssertException("No compressor recipes loaded");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ic2port.release_gate", timeoutTicks = 100)
    public static void steamFluidBaseline(final GameTestHelper helper) {
        if (ModFluids.STEAM.get() == null || ModFluids.STEAM_FLOWING.get() == null) {
            throw new GameTestAssertException("Steam fluid entries are missing");
        }
        if (ModFluids.STEAM_TYPE.get().getDescriptionId().isBlank()) {
            throw new GameTestAssertException("Steam fluid type is not initialized");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "ic2port.release_gate", timeoutTicks = 100)
    public static void teleporterEnergyCostSanity(final GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        BlockPos source = new BlockPos(0, 64, 0);
        BlockPos near = new BlockPos(8, 64, 0);
        BlockPos far = new BlockPos(64, 64, 0);

        double nearDistance = TeleporterCostHelper.calculateDistance(source, Level.OVERWORLD, near, Level.OVERWORLD);
        double farDistance = TeleporterCostHelper.calculateDistance(source, Level.OVERWORLD, far, Level.OVERWORLD);
        if (!(nearDistance > 0.0D && farDistance > nearDistance)) {
            throw new GameTestAssertException("Teleporter distance formula is not monotonic");
        }

        var fake = FakePlayerFactory.getMinecraft(level);
        double nearCost = TeleporterCostHelper.calculateEuCost(fake, nearDistance);
        double farCost = TeleporterCostHelper.calculateEuCost(fake, farDistance);
        if (!(nearCost > 0.0D && farCost > nearCost)) {
            throw new GameTestAssertException("Teleporter EU cost does not increase with distance");
        }

        if (TeleporterBlockEntity.ENERGY_CAPACITY <= 0.0D
                || BlockEntityRegistry.TELEPORTER_BE.get() == null) {
            throw new GameTestAssertException("Teleporter energy baseline is not initialized");
        }

        helper.succeed();
    }
}

