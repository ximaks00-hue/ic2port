package dev.ic2port.datagen;

import dev.ic2port.Reference;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates item model JSON files.
 * <p>
 * Stub — item models will be added when items are registered.
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Intentionally empty — item models will be registered alongside ItemRegistry entries.
    }
}
