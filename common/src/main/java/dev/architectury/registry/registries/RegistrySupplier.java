package dev.architectury.registry.registries;

import java.util.function.Supplier;

/**
 * Stub for dev.architectury.registry.registries.RegistrySupplier.
 */
public class RegistrySupplier<T> implements Supplier<T> {
    private final T value;

    public RegistrySupplier(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
