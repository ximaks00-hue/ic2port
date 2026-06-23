package dev.ic2port.tube;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * In-flight item inside a tube segment with travel progress for client animation.
 */
public final class TransportedItem {

    public static final byte DEFAULT_SPEED = 12;

    private ItemStack stack = ItemStack.EMPTY;
    private byte progress;
    private byte speed = DEFAULT_SPEED;
    private boolean hovering;
    @Nullable
    private Direction entryDirection;
    @Nullable
    private Direction exportDirection;
    @Nullable
    private DyeColor color;

    public TransportedItem() {
    }

    public TransportedItem(final ItemStack stack, final Direction entryDirection) {
        this.stack = stack.copy();
        this.entryDirection = entryDirection;
    }

    public static TransportedItem load(final CompoundTag tag) {
        TransportedItem item = new TransportedItem();
        item.stack = ItemStack.of(tag.getCompound("Stack"));
        item.progress = tag.getByte("Progress");
        item.speed = tag.getByte("Speed");
        if (tag.contains("Entry")) {
            item.entryDirection = Direction.from3DDataValue(tag.getInt("Entry"));
        }
        if (tag.contains("Export")) {
            item.exportDirection = Direction.from3DDataValue(tag.getInt("Export"));
        }
        if (tag.contains("Color")) {
            item.color = DyeColor.byId(tag.getInt("Color"));
        }
        item.hovering = tag.getBoolean("Hovering");
        return item.isEmpty() ? null : item;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("Stack", stack.save(new CompoundTag()));
        tag.putByte("Progress", progress);
        tag.putByte("Speed", speed);
        if (entryDirection != null) {
            tag.putInt("Entry", entryDirection.get3DDataValue());
        }
        if (exportDirection != null) {
            tag.putInt("Export", exportDirection.get3DDataValue());
        }
        if (color != null) {
            tag.putInt("Color", color.getId());
        }
        tag.putBoolean("Hovering", hovering);
        return tag;
    }

    public ItemStack getStack() {
        return stack;
    }

    public byte getProgress() {
        return progress;
    }

    @Nullable
    public Direction getEntryDirection() {
        return entryDirection;
    }

    @Nullable
    public Direction getExportDirection() {
        return exportDirection;
    }

    @Nullable
    public DyeColor getColor() {
        return color;
    }

    public void setColor(@Nullable final DyeColor color) {
        this.color = color;
    }

    public void setExportDirection(@Nullable final Direction exportDirection) {
        this.exportDirection = exportDirection;
    }

    public byte getSpeed() {
        return speed;
    }

    public void setSpeed(final byte speed) {
        this.speed = speed;
    }

    public boolean isHovering() {
        return hovering;
    }

    public void setHovering(final boolean hovering) {
        this.hovering = hovering;
    }

    public Direction getTravelDirection() {
        if (exportDirection != null) {
            return exportDirection;
        }
        return entryDirection != null ? entryDirection.getOpposite() : Direction.NORTH;
    }

    public void advance() {
        dev.ic2port.util.TubeGravityHelper.applyGravity(this);
        progress = (byte) Math.min(100, progress + speed);
    }

    public boolean isReady() {
        return progress >= 100;
    }

    public void resetProgress() {
        progress = 0;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public float getRenderOffset() {
        return (progress / 100.0F) - 0.5F;
    }
}
