package dev.ic2port.datagen;

import dev.ic2port.Reference;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates blockstate and block model JSON files.
 * <p>
 * Stub — block models will be added when blocks are registered.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Intentionally empty — blockstates will be registered alongside BlockRegistry entries.
    }
}
