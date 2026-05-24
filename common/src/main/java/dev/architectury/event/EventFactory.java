package dev.architectury.event;

/**
 * Stub for dev.architectury.event.EventFactory.
 */
public class EventFactory {
    public static <T> Event<T> createEventResult() {
        return new Event<>();
    }
}
