package dev.ic2port.datagen;

import dev.ic2port.block.BatBoxBlock;
import dev.ic2port.block.CopperCableBlock;
import dev.ic2port.block.EVTransformerBlock;
import dev.ic2port.block.GlassFiberCableBlock;
import dev.ic2port.block.GoldCableBlock;
import dev.ic2port.block.HvCableBlock;
import dev.ic2port.block.LVTransformerBlock;
import dev.ic2port.block.MFEBlock;
import dev.ic2port.block.MFSUBlock;
import dev.ic2port.block.MVTransformerBlock;
import dev.ic2port.block.GeothermalGeneratorBlock;
import dev.ic2port.block.SolidFuelGeneratorBlock;
import dev.ic2port.block.WindMillBlock;
import dev.ic2port.block.RubberWoodBlock;
import dev.ic2port.Reference;
import dev.ic2port.setup.BlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates blockstate and block model JSON files.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(
                BlockRegistry.CREATIVE_GENERATOR.get(),
                models().cubeAll("creative_generator", modLoc("block/creative_generator")));
        simpleBlockWithItem(
                BlockRegistry.HV_CREATIVE_GENERATOR.get(),
                models().orientable(
                        "hv_creative_generator",
                        modLoc("block/hv_creative_generator_top"),
                        modLoc("block/hv_creative_generator_side"),
                        modLoc("block/hv_creative_generator_side")));
        simpleBlockWithItem(
                BlockRegistry.MACERATOR.get(),
                models().orientable(
                        "macerator",
                        modLoc("block/macerator_top"),
                        modLoc("block/macerator_front"),
                        modLoc("block/basic_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.RECYCLER.get(),
                models().orientable(
                        "recycler",
                        modLoc("block/macerator_top"),
                        modLoc("block/recycler_front"),
                        modLoc("block/basic_machine_casing")));

        registerSolidFuelGeneratorStates();
        registerGeothermalGeneratorStates();
        registerSolarPanelStates();
        registerWindMillStates();
        registerWaterMillStates();
        registerBatBoxStates();
        registerMfeStates();
        registerMfsuStates();
        registerLvTransformerStates();
        registerMvTransformerStates();
        registerEvTransformerStates();

        simpleBlockWithItem(
                BlockRegistry.TIN_ORE.get(),
                models().cubeAll("tin_ore", modLoc("block/tin_ore")));
        simpleBlockWithItem(
                BlockRegistry.DEEPSLATE_TIN_ORE.get(),
                models().cubeAll("deepslate_tin_ore", modLoc("block/deepslate_tin_ore")));
        simpleBlockWithItem(
                BlockRegistry.URANIUM_ORE.get(),
                models().cubeAll("uranium_ore", modLoc("block/uranium_ore")));
        simpleBlockWithItem(
                BlockRegistry.DEEPSLATE_URANIUM_ORE.get(),
                models().cubeAll("deepslate_uranium_ore", modLoc("block/deepslate_uranium_ore")));

        simpleBlockWithItem(
                BlockRegistry.EXTRACTOR.get(),
                models().orientable(
                        "extractor",
                        modLoc("block/basic_machine_casing"),
                        modLoc("block/extractor_front"),
                        modLoc("block/basic_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.COMPRESSOR.get(),
                models().orientable(
                        "compressor",
                        modLoc("block/basic_machine_casing"),
                        modLoc("block/compressor_front"),
                        modLoc("block/basic_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.ELECTRIC_FURNACE.get(),
                models().orientable(
                        "electric_furnace",
                        modLoc("block/basic_machine_casing"),
                        modLoc("block/electric_furnace_front"),
                        modLoc("block/basic_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.INDUCTION_FURNACE.get(),
                models().orientable(
                        "induction_furnace",
                        modLoc("block/advanced_machine_casing"),
                        modLoc("block/induction_furnace_front"),
                        modLoc("block/advanced_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.METAL_FORMER.get(),
                models().orientable(
                        "metal_former",
                        modLoc("block/advanced_machine_casing"),
                        modLoc("block/metal_former_front"),
                        modLoc("block/advanced_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.CHARGE_PAD.get(),
                models().cubeBottomTop(
                        "charge_pad",
                        modLoc("block/charge_pad_side"),
                        modLoc("block/charge_pad_bottom"),
                        modLoc("block/charge_pad_top")));
        simpleBlockWithItem(
                BlockRegistry.THERMAL_CENTRIFUGE.get(),
                models().orientable(
                        "thermal_centrifuge",
                        modLoc("block/thermal_centrifuge_top"),
                        modLoc("block/thermal_centrifuge_front"),
                        modLoc("block/advanced_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.MASS_FABRICATOR.get(),
                models().cubeBottomTop(
                        "mass_fabricator",
                        modLoc("block/mass_fabricator_side"),
                        modLoc("block/mass_fabricator_bottom"),
                        modLoc("block/mass_fabricator_top")));
        simpleBlockWithItem(
                BlockRegistry.NUCLEAR_REACTOR.get(),
                models().orientable(
                        "nuclear_reactor",
                        modLoc("block/nuclear_reactor_top"),
                        modLoc("block/nuclear_reactor_front"),
                        modLoc("block/nuclear_reactor_side")));
        simpleBlockWithItem(
                BlockRegistry.REACTOR_CHAMBER.get(),
                models().cubeAll("reactor_chamber", modLoc("block/reactor_chamber")));
        simpleBlockWithItem(
                BlockRegistry.CONTAMINATED_SOIL.get(),
                models().cubeAll("contaminated_soil", modLoc("block/contaminated_soil")));
        simpleBlockWithItem(
                BlockRegistry.BASIC_MACHINE_CASING.get(),
                models().cubeAll("basic_machine_casing", modLoc("block/basic_machine_casing")));
        simpleBlockWithItem(
                BlockRegistry.ADVANCED_MACHINE_CASING.get(),
                models().cubeAll("advanced_machine_casing", modLoc("block/advanced_machine_casing")));
        registerRubberWoodBlockStates();
        final ModelFile rubberLeaves = models().withExistingParent("rubber_leaves", "block/leaves")
                .texture("all", modLoc("block/rubber_leaves"));
        getMultipartBuilder(BlockRegistry.RUBBER_LEAVES.get())
                .part()
                .modelFile(rubberLeaves)
                .addModel();
        simpleBlockItem(BlockRegistry.RUBBER_LEAVES.get(), rubberLeaves);
        simpleBlockWithItem(
                BlockRegistry.RUBBER_SAPLING.get(),
                models().cross("rubber_sapling", modLoc("block/rubber_sapling")));

        registerCopperCableMultipart();
        registerGoldCableMultipart();
        registerHvCableMultipart();
        registerGlassFiberCableMultipart();
    }

    private void registerRubberWoodBlockStates() {
        Block block = BlockRegistry.RUBBER_WOOD.get();
        ModelFile model = models().cubeColumn(
                "rubber_wood",
                modLoc("block/rubber_wood"),
                modLoc("block/rubber_wood"));
        ModelFile resinModel = models().cubeColumn(
                "rubber_wood_resin",
                modLoc("block/rubber_wood_resin"),
                modLoc("block/rubber_wood"));

        getVariantBuilder(block).forAllStates(state -> {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            boolean resin = state.getValue(RubberWoodBlock.RESIN) && !state.getValue(RubberWoodBlock.DEPLETED);
            ConfiguredModel.Builder<?> builder = ConfiguredModel.builder()
                    .modelFile(resin ? resinModel : model);
            switch (axis) {
                case X -> builder.rotationX(90).rotationY(90);
                case Y -> {
                }
                case Z -> builder.rotationX(90);
            }
            return builder.build();
        });
        simpleBlockItem(block, model);
    }

    private void registerMfsuStates() {
        ModelFile model = models().orientable(
                "mfsu",
                modLoc("block/mfsu_side"),
                modLoc("block/mfsu_front"),
                modLoc("block/mfsu_side"));

        getVariantBuilder(BlockRegistry.MFSU.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(MFSUBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerMvTransformerStates() {
        ModelFile model = models().orientable(
                "mv_transformer",
                modLoc("block/mv_transformer_side"),
                modLoc("block/mv_transformer_front"),
                modLoc("block/mv_transformer_side"));

        getVariantBuilder(BlockRegistry.MV_TRANSFORMER.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(MVTransformerBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerEvTransformerStates() {
        ModelFile model = models().orientable(
                "ev_transformer",
                modLoc("block/ev_transformer_side"),
                modLoc("block/ev_transformer_front"),
                modLoc("block/ev_transformer_side"));

        getVariantBuilder(BlockRegistry.EV_TRANSFORMER.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(EVTransformerBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerHvCableMultipart() {
        ModelFile coreModel = models().getExistingFile(modLoc("block/hv_cable_core"));
        ModelFile armModel = models().getExistingFile(modLoc("block/hv_cable_arm"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(BlockRegistry.HV_CABLE.get());
        builder.part().modelFile(coreModel).addModel().end();

        addHvCableArm(builder, armModel, Direction.NORTH);
        addHvCableArm(builder, armModel, Direction.SOUTH);
        addHvCableArm(builder, armModel, Direction.EAST);
        addHvCableArm(builder, armModel, Direction.WEST);
        addHvCableArm(builder, armModel, Direction.UP);
        addHvCableArm(builder, armModel, Direction.DOWN);
    }

    private void addHvCableArm(
            final MultiPartBlockStateBuilder builder,
            final ModelFile armModel,
            final Direction direction) {
        ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> partBuilder = builder.part().modelFile(armModel);
        int xRot = xRotationFor(direction);
        int yRot = yRotationFor(direction);
        if (xRot != 0) {
            partBuilder.rotationX(xRot);
        }
        if (yRot != 0) {
            partBuilder.rotationY(yRot);
        }
        partBuilder.addModel()
                .condition(hvCablePropertyFor(direction), true)
                .end();
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty hvCablePropertyFor(
            final Direction direction) {
        return switch (direction) {
            case NORTH -> HvCableBlock.NORTH;
            case SOUTH -> HvCableBlock.SOUTH;
            case EAST -> HvCableBlock.EAST;
            case WEST -> HvCableBlock.WEST;
            case UP -> HvCableBlock.UP;
            case DOWN -> HvCableBlock.DOWN;
        };
    }

    private void registerGlassFiberCableMultipart() {
        ModelFile coreModel = models().getExistingFile(modLoc("block/glass_fiber_cable_core"));
        ModelFile armModel = models().getExistingFile(modLoc("block/glass_fiber_cable_arm"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(BlockRegistry.GLASS_FIBER_CABLE.get());
        builder.part().modelFile(coreModel).addModel().end();

        addGlassFiberCableArm(builder, armModel, Direction.NORTH);
        addGlassFiberCableArm(builder, armModel, Direction.SOUTH);
        addGlassFiberCableArm(builder, armModel, Direction.EAST);
        addGlassFiberCableArm(builder, armModel, Direction.WEST);
        addGlassFiberCableArm(builder, armModel, Direction.UP);
        addGlassFiberCableArm(builder, armModel, Direction.DOWN);
    }

    private void addGlassFiberCableArm(
            final MultiPartBlockStateBuilder builder,
            final ModelFile armModel,
            final Direction direction) {
        ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> partBuilder = builder.part().modelFile(armModel);
        int xRot = xRotationFor(direction);
        int yRot = yRotationFor(direction);
        if (xRot != 0) {
            partBuilder.rotationX(xRot);
        }
        if (yRot != 0) {
            partBuilder.rotationY(yRot);
        }
        partBuilder.addModel()
                .condition(glassFiberCablePropertyFor(direction), true)
                .end();
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty glassFiberCablePropertyFor(
            final Direction direction) {
        return switch (direction) {
            case NORTH -> GlassFiberCableBlock.NORTH;
            case SOUTH -> GlassFiberCableBlock.SOUTH;
            case EAST -> GlassFiberCableBlock.EAST;
            case WEST -> GlassFiberCableBlock.WEST;
            case UP -> GlassFiberCableBlock.UP;
            case DOWN -> GlassFiberCableBlock.DOWN;
        };
    }

    private void registerMfeStates() {
        ModelFile model = models().orientable(
                "mfe",
                modLoc("block/mfe_side"),
                modLoc("block/mfe_front"),
                modLoc("block/mfe_side"));

        getVariantBuilder(BlockRegistry.MFE.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(MFEBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerLvTransformerStates() {
        ModelFile model = models().orientable(
                "lv_transformer",
                modLoc("block/lv_transformer_side"),
                modLoc("block/lv_transformer_front"),
                modLoc("block/lv_transformer_side"));

        getVariantBuilder(BlockRegistry.LV_TRANSFORMER.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(LVTransformerBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerGoldCableMultipart() {
        ModelFile coreModel = models().getExistingFile(modLoc("block/gold_cable_core"));
        ModelFile armModel = models().getExistingFile(modLoc("block/gold_cable_arm"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(BlockRegistry.GOLD_CABLE.get());
        builder.part().modelFile(coreModel).addModel().end();

        addGoldCableArm(builder, armModel, Direction.NORTH);
        addGoldCableArm(builder, armModel, Direction.SOUTH);
        addGoldCableArm(builder, armModel, Direction.EAST);
        addGoldCableArm(builder, armModel, Direction.WEST);
        addGoldCableArm(builder, armModel, Direction.UP);
        addGoldCableArm(builder, armModel, Direction.DOWN);
    }

    private void addGoldCableArm(
            final MultiPartBlockStateBuilder builder,
            final ModelFile armModel,
            final Direction direction) {
        ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> partBuilder = builder.part().modelFile(armModel);
        int xRot = xRotationFor(direction);
        int yRot = yRotationFor(direction);
        if (xRot != 0) {
            partBuilder.rotationX(xRot);
        }
        if (yRot != 0) {
            partBuilder.rotationY(yRot);
        }
        partBuilder.addModel()
                .condition(goldCablePropertyFor(direction), true)
                .end();
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty goldCablePropertyFor(
            final Direction direction) {
        return switch (direction) {
            case NORTH -> GoldCableBlock.NORTH;
            case SOUTH -> GoldCableBlock.SOUTH;
            case EAST -> GoldCableBlock.EAST;
            case WEST -> GoldCableBlock.WEST;
            case UP -> GoldCableBlock.UP;
            case DOWN -> GoldCableBlock.DOWN;
        };
    }

    private void registerWaterMillStates() {
        ModelFile model = models().cubeBottomTop(
                "water_mill",
                modLoc("block/water_mill_side"),
                modLoc("block/basic_machine_casing"),
                modLoc("block/water_mill_top"));
        simpleBlockWithItem(BlockRegistry.WATER_MILL.get(), model);
    }

    private void registerWindMillStates() {
        ModelFile model = models().orientable(
                "wind_mill",
                modLoc("block/wind_mill_top"),
                modLoc("block/wind_mill_front"),
                modLoc("block/wind_mill_bottom"));

        getVariantBuilder(BlockRegistry.WIND_MILL.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationY(((int) state.getValue(WindMillBlock.FACING).toYRot() + 180) % 360)
                        .build());
    }

    private void registerSolarPanelStates() {
        ModelFile model = models().cubeBottomTop(
                "solar_panel",
                modLoc("block/solar_panel_side"),
                modLoc("block/basic_machine_casing"),
                modLoc("block/solar_panel_top"));
        simpleBlockWithItem(BlockRegistry.SOLAR_PANEL.get(), model);
    }

    private void registerSolidFuelGeneratorStates() {
        ModelFile offModel = models().orientable(
                "solid_fuel_generator",
                modLoc("block/basic_machine_casing"),
                modLoc("block/solid_fuel_generator_front"),
                modLoc("block/basic_machine_casing"));
        ModelFile onModel = models().orientable(
                "solid_fuel_generator_on",
                modLoc("block/basic_machine_casing"),
                modLoc("block/solid_fuel_generator_front_on"),
                modLoc("block/basic_machine_casing"));

        getVariantBuilder(BlockRegistry.SOLID_FUEL_GENERATOR.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(SolidFuelGeneratorBlock.LIT) ? onModel : offModel)
                        .rotationY(((int) state.getValue(HorizontalDirectionalBlock.FACING).toYRot() + 180) % 360)
                        .build());
    }

    private void registerGeothermalGeneratorStates() {
        ModelFile offModel = models().orientable(
                "geothermal_generator",
                modLoc("block/geothermal_generator_top"),
                modLoc("block/geothermal_generator_side"),
                modLoc("block/basic_machine_casing"));
        ModelFile onModel = models().orientable(
                "geothermal_generator_on",
                modLoc("block/geothermal_generator_top_on"),
                modLoc("block/geothermal_generator_side_on"),
                modLoc("block/basic_machine_casing"));

        getVariantBuilder(BlockRegistry.GEOTHERMAL_GENERATOR.get())
                .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(state.getValue(GeothermalGeneratorBlock.LIT) ? onModel : offModel)
                        .rotationY(((int) state.getValue(HorizontalDirectionalBlock.FACING).toYRot() + 180) % 360)
                        .build());
    }

    private void registerBatBoxStates() {
        ModelFile model = models().orientable(
                "batbox",
                modLoc("block/batbox_side"),
                modLoc("block/batbox_front"),
                modLoc("block/batbox_side"));

        getVariantBuilder(BlockRegistry.BATBOX.get())
                .forAllStates(state -> {
                    Direction facing = state.getValue(BatBoxBlock.FACING);
                    ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
                    switch (facing) {
                        case UP -> builder.rotationX(270);
                        case DOWN -> builder.rotationX(90);
                        case SOUTH -> builder.rotationY(180);
                        case WEST -> builder.rotationY(90);
                        case EAST -> builder.rotationY(270);
                        default -> {
                        }
                    }
                    return builder.build();
                });
    }

    private void registerCopperCableMultipart() {
        ModelFile coreModel = models().getExistingFile(modLoc("block/copper_cable_core"));
        ModelFile armModel = models().getExistingFile(modLoc("block/copper_cable_arm"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(BlockRegistry.COPPER_CABLE.get());
        builder.part().modelFile(coreModel).addModel().end();

        addCableArm(builder, armModel, Direction.NORTH);
        addCableArm(builder, armModel, Direction.SOUTH);
        addCableArm(builder, armModel, Direction.EAST);
        addCableArm(builder, armModel, Direction.WEST);
        addCableArm(builder, armModel, Direction.UP);
        addCableArm(builder, armModel, Direction.DOWN);
    }

    private void addCableArm(
            final MultiPartBlockStateBuilder builder,
            final ModelFile armModel,
            final Direction direction) {
        ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> partBuilder = builder.part().modelFile(armModel);
        int xRot = xRotationFor(direction);
        int yRot = yRotationFor(direction);
        if (xRot != 0) {
            partBuilder.rotationX(xRot);
        }
        if (yRot != 0) {
            partBuilder.rotationY(yRot);
        }
        partBuilder.addModel()
                .condition(statePropertyFor(direction), true)
                .end();
    }

    private static int xRotationFor(final Direction direction) {
        return switch (direction) {
            case UP -> 270;
            case DOWN -> 90;
            default -> 0;
        };
    }

    private static int yRotationFor(final Direction direction) {
        return switch (direction) {
            case SOUTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty statePropertyFor(final Direction direction) {
        return switch (direction) {
            case NORTH -> CopperCableBlock.NORTH;
            case SOUTH -> CopperCableBlock.SOUTH;
            case EAST -> CopperCableBlock.EAST;
            case WEST -> CopperCableBlock.WEST;
            case UP -> CopperCableBlock.UP;
            case DOWN -> CopperCableBlock.DOWN;
        };
    }
}
