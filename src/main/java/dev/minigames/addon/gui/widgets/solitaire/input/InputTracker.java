package dev.minigames.addon.gui.widgets.solitaire.input;

import java.util.HashMap;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * @author Tas [0xTas] <root@0xTas.dev>
 **/
public class InputTracker {
    private final HashMap<Integer, Boolean> keyStates;

    public InputTracker() { this.keyStates = new HashMap<>(); }

    public void updateState(int key, boolean active) {
        keyStates.put(key, active);
    }

    public boolean isNotHeld(int key) {
        return !keyStates.getOrDefault(key, false);
    }

    public static boolean isKeyDown(int key) {
        if (mc == null || mc.getWindow() == null) return false;
        long handle = mc.getWindow().getHandle();
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }
}

