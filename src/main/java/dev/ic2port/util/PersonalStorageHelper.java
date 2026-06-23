package dev.ic2port.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared NBT and ACL helpers for {@link dev.ic2port.api.tiles.IPersonalStorage}.
 */
public final class PersonalStorageHelper {

    private PersonalStorageHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static List<UUID> readFriends(final CompoundTag tag) {
        List<UUID> friends = new ArrayList<>();
        if (!tag.contains("Friends", Tag.TAG_LIST)) {
            return friends;
        }
        ListTag list = tag.getList("Friends", Tag.TAG_COMPOUND);
        for (Tag entry : list) {
            if (entry instanceof CompoundTag compound && compound.hasUUID("Id")) {
                friends.add(compound.getUUID("Id"));
            }
        }
        return friends;
    }

    public static void writeFriends(final CompoundTag tag, final List<UUID> friends) {
        ListTag list = new ListTag();
        for (UUID uuid : friends) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", uuid);
            list.add(entry);
        }
        tag.put("Friends", list);
    }

    public static boolean canAccess(
            final Player player,
            final UUID ownerUuid,
            final List<UUID> friends) {
        if (player.isCreative()) {
            return true;
        }
        if (ownerUuid == null) {
            return true;
        }
        UUID playerId = player.getUUID();
        return playerId.equals(ownerUuid) || friends.contains(playerId);
    }
}
