package dev.ic2port.util;

import dev.ic2port.api.energy.IEnergyNode;
import dev.ic2port.setup.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks EU flow at a block while the EU Reader is in flow-measurement mode.
 */
public final class EuReaderFlowService {

    public static final int MEASURE_TICKS = 20;

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();

    private EuReaderFlowService() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean start(final Player player, final Level level, final BlockPos pos) {
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }
        if (!isEnergyBlock(level, pos)) {
            return false;
        }

        double startStored = readStoredEnergy(level, pos);
        SESSIONS.put(
                player.getUUID(),
                new Session(
                        player.getUUID(),
                        level.dimension(),
                        pos.immutable(),
                        startStored));
        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.flow.start", MEASURE_TICKS), true);
        return true;
    }

    public static boolean showProgress(final Player player, final BlockPos pos) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.pos.equals(pos)) {
            return false;
        }
        int elapsed = (int) Math.min(MEASURE_TICKS, session.ticksElapsed);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.flow.progress",
                        elapsed,
                        MEASURE_TICKS,
                        EuReaderHelper.formatEnergy(session.energyIn),
                        EuReaderHelper.formatEnergy(session.energyOut)),
                true);
        return true;
    }

    public static void cancel(final UUID playerId) {
        SESSIONS.remove(playerId);
    }

    public static void tickServer(final ServerLevel level) {
        if (SESSIONS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Session>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Session> entry = iterator.next();
            Session session = entry.getValue();
            if (!session.dimension.equals(level.dimension())) {
                continue;
            }

            session.ticksElapsed++;
            if (session.ticksElapsed < MEASURE_TICKS) {
                continue;
            }

            finish(level, session);
            iterator.remove();
        }
    }

    public static void recordTransfer(
            final Level level,
            final BlockPos source,
            final BlockPos target,
            final double amount) {
        if (level.isClientSide || amount <= 0.0D || SESSIONS.isEmpty()) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        for (Session session : SESSIONS.values()) {
            if (!session.dimension.equals(dimension)) {
                continue;
            }
            if (session.pos.equals(source)) {
                session.energyOut += amount;
            }
            if (session.pos.equals(target)) {
                session.energyIn += amount;
            }
        }
    }

    private static void finish(final ServerLevel level, final Session session) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(session.playerId);
        if (player == null) {
            return;
        }

        double endStored = readStoredEnergy(level, session.pos);
        double netStored = endStored - session.startStored;
        int ticks = MEASURE_TICKS;

        player.displayClientMessage(Component.translatable("message.ic2port.eu_reader.flow.done"), true);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.flow.in",
                        EuReaderHelper.formatEnergy(session.energyIn / ticks)),
                true);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.flow.out",
                        EuReaderHelper.formatEnergy(session.energyOut / ticks)),
                true);
        player.displayClientMessage(
                Component.translatable(
                        "message.ic2port.eu_reader.flow.net",
                        EuReaderHelper.formatEnergy(netStored / ticks)),
                true);
    }

    private static boolean isEnergyBlock(final Level level, final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        IEnergyNode node = blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, null).orElse(null);
        return node != null || blockEntity instanceof IEnergyNode;
    }

    private static double readStoredEnergy(final Level level, final BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return 0.0D;
        }
        IEnergyNode node = blockEntity.getCapability(ModCapabilities.ENERGY_NODE_CAPABILITY, null).orElse(null);
        if (node == null && blockEntity instanceof IEnergyNode direct) {
            node = direct;
        }
        return node == null ? 0.0D : node.getStoredEnergy();
    }

    private static final class Session {
        private final UUID playerId;
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;
        private final double startStored;
        private int ticksElapsed;
        private double energyIn;
        private double energyOut;

        private Session(
                final UUID playerId,
                final ResourceKey<Level> dimension,
                final BlockPos pos,
                final double startStored) {
            this.playerId = playerId;
            this.dimension = dimension;
            this.pos = pos;
            this.startStored = startStored;
        }
    }
}
