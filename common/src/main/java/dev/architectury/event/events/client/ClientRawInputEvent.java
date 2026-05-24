package dev.architectury.event.events.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;

/**
 * Stub for dev.architectury.event.events.client.ClientRawInputEvent.
 */
public class ClientRawInputEvent {
    public static final Event<KeyPressed> KEY_PRESSED = new Event<>();
    public static final Event<MouseClickedPre> MOUSE_CLICKED_PRE = new Event<>();

    @FunctionalInterface
    public interface KeyPressed {
        EventResult keyPressed(Minecraft client, int keyCode, int scanCode, int action, int modifiers);
    }

    @FunctionalInterface
    public interface MouseClickedPre {
        EventResult mouseClickedPre(Minecraft client, int button, int action, int mods);
    }
}
