package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.util.TeleportLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HV teleporter block — teleports players to a linked teleporter at an EU cost of 100 EU per 1000 m² (minimum 500 EU).
 */
public class TeleporterBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 500_000.0D;
    public static final int TIER = EnergyTier.HV;
    public static final double EU_PER_BLOCK_DISTANCE = 0.1D;
    public static final double MIN_EU_COST = 500.0D;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);
    private double storedEnergy;

    public TeleporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.TELEPORTER_BE.get(), pos, state);
    }

    /**
     * Attempts to teleport the given player to {@code link}.
     * Returns true if teleportation succeeded.
     */
    public boolean teleportPlayer(final Player player, final TeleportLink link) {
        return tryTeleportPlayer(player, link) == null;
    }

    /** @return failure reason key suffix, or null on success */
    @Nullable
    public String tryTeleportPlayer(final Player player, final TeleportLink link) {
        if (level == null || level.isClientSide) {
            return "invalid_target";
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return "invalid_target";
        }

        ServerLevel destLevel = serverLevel.getServer().getLevel(link.dimension());
        if (destLevel == null) {
            return "invalid_target";
        }

        BlockPos destination = link.pos();
        BlockEntity destBe = destLevel.getBlockEntity(destination);
        if (!(destBe instanceof TeleporterBlockEntity)) {
            return "invalid_target";
        }

        double dx = destination.getX() - worldPosition.getX();
        double dy = destination.getY() - worldPosition.getY();
        double dz = destination.getZ() - worldPosition.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double euCost = Math.max(MIN_EU_COST, dist * EU_PER_BLOCK_DISTANCE);
        if (!destLevel.dimension().equals(serverLevel.dimension())) {
            euCost = Math.max(euCost, MIN_EU_COST * 4.0D);
        }

        if (storedEnergy < euCost) {
            return "no_energy";
        }

        storedEnergy -= euCost;
        setChanged();

        double tx = destination.getX() + 0.5;
        double ty = destination.getY() + 1.0;
        double tz = destination.getZ() + 0.5;

        if (destLevel.dimension().equals(serverLevel.dimension())) {
            player.teleportTo(tx, ty, tz);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(destLevel, tx, ty, tz, serverPlayer.getYRot(), serverPlayer.getXRot());
        } else {
            return "invalid_target";
        }
        return null;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D) {
            return amount;
        }
        if (tier > getTier()) {
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        storedEnergy += accepted;
        setChanged();
        return amount - accepted;
    }

    @Override public double getCapacity() { return ENERGY_CAPACITY; }
    @Override public double getStoredEnergy() { return storedEnergy; }
    @Override public int getTier() { return TIER; }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("StoredEnergy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("StoredEnergy"), ENERGY_CAPACITY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(final @NotNull Capability<T> cap,
                                                       final @Nullable Direction side) {
        if (cap == ModCapabilities.ENERGY_NODE_CAPABILITY) return energyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
