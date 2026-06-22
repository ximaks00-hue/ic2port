package dev.ic2port.blockentity;

import dev.ic2port.setup.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Optional camouflage block state for construction foam (obscurator).
 */
public class ConstructionFoamBlockEntity extends BlockEntity {

    @Nullable
    private BlockState disguise;

    public ConstructionFoamBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CONSTRUCTION_FOAM_BE.get(), pos, state);
    }

    @Nullable
    public BlockState getDisguise() {
        return disguise;
    }

    public void setDisguise(final @Nullable BlockState disguise) {
        this.disguise = disguise;
        setChanged();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        if (disguise != null) {
            tag.put("Disguise", NbtUtils.writeBlockState(disguise));
        }
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Disguise")) {
            disguise = NbtUtils.readBlockState(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
                    tag.getCompound("Disguise"));
        } else {
            disguise = null;
        }
    }
}
