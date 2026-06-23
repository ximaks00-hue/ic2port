package dev.ic2port.blockentity;

import dev.ic2port.api.tiles.IPersonalStorage;
import dev.ic2port.util.PersonalStorageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base block entity for owner-bound storage with a friends access list.
 */
public abstract class AbstractPersonalStorageBlockEntity extends BlockEntity implements IPersonalStorage {

    @Nullable
    private UUID ownerUuid;
    private final List<UUID> friends = new ArrayList<>();

    protected AbstractPersonalStorageBlockEntity(
            final BlockEntityType<?> type,
            final BlockPos pos,
            final BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    @Override
    public void bindOwner(final Player player) {
        if (ownerUuid == null) {
            ownerUuid = player.getUUID();
            setChanged();
        }
    }

    @Override
    public List<UUID> getFriends() {
        return List.copyOf(friends);
    }

    @Override
    public boolean canAccess(final Player player) {
        return PersonalStorageHelper.canAccess(player, ownerUuid, friends);
    }

    @Override
    public boolean isFriend(final UUID uuid) {
        return friends.contains(uuid);
    }

    @Override
    public void addFriend(final UUID uuid) {
        if (ownerUuid != null && ownerUuid.equals(uuid)) {
            return;
        }
        if (!friends.contains(uuid)) {
            friends.add(uuid);
            setChanged();
        }
    }

    @Override
    public void removeFriend(final UUID uuid) {
        if (friends.remove(uuid)) {
            setChanged();
        }
    }

    protected void savePersonalData(final CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        PersonalStorageHelper.writeFriends(tag, friends);
    }

    protected void loadPersonalData(final CompoundTag tag) {
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        friends.clear();
        friends.addAll(PersonalStorageHelper.readFriends(tag));
    }
}
