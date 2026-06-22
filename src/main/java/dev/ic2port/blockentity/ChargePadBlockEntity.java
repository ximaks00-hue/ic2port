package dev.ic2port.blockentity;

import dev.ic2port.api.energy.EnergyTier;
import dev.ic2port.api.energy.IEnergyAcceptor;
import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.BlockEntityRegistry;
import dev.ic2port.setup.ModCapabilities;
import dev.ic2port.setup.ModConfig;
import dev.ic2port.util.ItemEnergyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * MV charge pad — charges worn electric armor and held tools while the player stands on it.
 */
public class ChargePadBlockEntity extends BlockEntity implements IEnergyAcceptor {

    public static final double ENERGY_CAPACITY = 40_000.0D;
    public static final double MAX_CHARGE_PER_TICK = 128.0D;
    public static final int TIER = EnergyTier.MV;

    private final LazyOptional<IEnergyNode> energyOptional = LazyOptional.of(() -> this);

    private double storedEnergy;
    private boolean destroyedByOverload;

    public ChargePadBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntityRegistry.CHARGE_PAD_BE.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final ChargePadBlockEntity chargePad) {
        chargePad.tickServer();
    }

    private void tickServer() {
        if (level == null || level.isClientSide || destroyedByOverload || storedEnergy <= 0.0D) {
            return;
        }

        double tickBudget = Math.min(storedEnergy, MAX_CHARGE_PER_TICK);
        AABB area = new AABB(worldPosition).inflate(0.1D, 0.5D, 0.1D).move(0.0D, 1.0D, 0.0D);
        List<Player> players = new ArrayList<>();
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (isStandingOnPad(player)) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            return;
        }

        double perPlayerBudget = tickBudget / players.size();
        for (Player player : players) {
            if (storedEnergy <= 0.0D) {
                break;
            }
            double remaining = Math.min(perPlayerBudget, storedEnergy);
            chargePlayer(player, remaining);
        }
    }

    private void chargePlayer(final Player player, final double tickBudget) {
        double budget = tickBudget;

        budget -= chargeStack(player, player.getMainHandItem(), budget);
        if (budget <= 0.0D) {
            return;
        }

        budget -= chargeStack(player, player.getOffhandItem(), budget);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR || budget <= 0.0D) {
                continue;
            }
            budget -= chargeStack(player, player.getItemBySlot(slot), budget);
        }
    }

    private double chargeStack(final Player player, final ItemStack stack, final double budget) {
        if (budget <= 0.0D || !ItemEnergyHelper.canCharge(stack, TIER)) {
            return 0.0D;
        }
        double transferred = ItemEnergyHelper.chargeItem(stack, budget, TIER);
        if (transferred > 0.0D) {
            storedEnergy -= transferred;
            setChanged();
            player.getInventory().setChanged();
        }
        return transferred;
    }

    @Override
    public double injectEnergy(final Direction directionFrom, final double amount, final int tier) {
        if (level == null || level.isClientSide || amount <= 0.0D || destroyedByOverload) {
            return amount;
        }
        if (tier > TIER) {
            explode(tier);
            return amount;
        }
        double space = ENERGY_CAPACITY - storedEnergy;
        double accepted = Math.min(amount, space);
        if (accepted > 0.0D) {
            storedEnergy += accepted;
            setChanged();
        }
        return amount - accepted;
    }

    private void explode(final int incomingTier) {
        if (level == null || level.isClientSide || destroyedByOverload) {
            return;
        }
        destroyedByOverload = true;
        storedEnergy = 0.0D;

        final float radius = ModConfig.EXPLOSION_BASE_RADIUS.get().floatValue()
                + (incomingTier - TIER) * 1.5F;
        final double centerX = worldPosition.getX() + 0.5D;
        final double centerY = worldPosition.getY() + 0.5D;
        final double centerZ = worldPosition.getZ() + 0.5D;

        level.removeBlock(worldPosition, false);
        level.explode(null, centerX, centerY, centerZ, radius, Level.ExplosionInteraction.BLOCK);
    }

    private boolean isStandingOnPad(final Player player) {
        return player.blockPosition().below().equals(worldPosition);
    }

    @Override
    public int getTier() {
        return TIER;
    }

    @Override
    public double getStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public double getCapacity() {
        return ENERGY_CAPACITY;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("Energy", storedEnergy);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        storedEnergy = Math.min(tag.getDouble("Energy"), ENERGY_CAPACITY);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            final @NotNull Capability<T> capability,
            final @Nullable Direction side) {
        if (capability == ModCapabilities.ENERGY_NODE_CAPABILITY) {
            return energyOptional.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }
}
