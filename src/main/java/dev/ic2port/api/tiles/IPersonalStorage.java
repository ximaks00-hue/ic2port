package dev.ic2port.api.tiles;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Access-controlled personal storage (chest, tank, etc.).
 */
public interface IPersonalStorage {

    @Nullable
    UUID getOwnerUuid();

    void bindOwner(Player player);

    List<UUID> getFriends();

    boolean canAccess(Player player);

    boolean isFriend(UUID uuid);

    void addFriend(UUID uuid);

    void removeFriend(UUID uuid);
}
