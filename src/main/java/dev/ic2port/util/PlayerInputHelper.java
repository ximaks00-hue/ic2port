package dev.ic2port.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Reads {@link LivingEntity#jumping} from server tick handlers outside the entity package.
 */
public final class PlayerInputHelper {

    @Nullable
    private static final Field JUMPING_FIELD = resolveJumpingField();

    private PlayerInputHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    private static Field resolveJumpingField() {
        try {
            Field field = LivingEntity.class.getDeclaredField("jumping");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }

    public static boolean isJumping(final Player player) {
        if (JUMPING_FIELD == null) {
            return false;
        }
        try {
            return JUMPING_FIELD.getBoolean(player);
        } catch (IllegalAccessException exception) {
            return false;
        }
    }
}
