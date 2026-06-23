package dev.ic2port.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ic2port.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyBindings {

    public static final String CATEGORY = "key.categories." + Reference.MOD_ID;

    public static final KeyMapping TOGGLE_ADVANCED_DRILL_MODE = new KeyMapping(
            "key." + Reference.MOD_ID + ".toggle_advanced_drill_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY);

    public static final KeyMapping TOGGLE_QUANTUM_NIGHT_VISION = new KeyMapping(
            "key." + Reference.MOD_ID + ".toggle_quantum_night_vision",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY);

    public static final KeyMapping TOGGLE_MINING_LASER_MODE = new KeyMapping(
            "key." + Reference.MOD_ID + ".toggle_mining_laser_mode",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            CATEGORY);

    private ClientKeyBindings() {
        throw new UnsupportedOperationException("Utility class");
    }
}
