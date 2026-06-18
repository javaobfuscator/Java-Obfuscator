package dev.hardobfuscator.core.di;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Lightweight service locator implementing dependency injection for the engine.
 */
public final class ServiceRegistry {

    private static final ServiceRegistry INSTANCE = new ServiceRegistry();
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new ConcurrentHashMap<>();

    private ServiceRegistry() {
    }

    public static ServiceRegistry getInstance() {
        return INSTANCE;
    }

    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
    }

    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        Object singleton = singletons.get(type);
        if (singleton != null) {
            return (T) singleton;
        }
        Supplier<?> factory = factories.get(type);
        if (factory != null) {
            T created = (T) factory.get();
            singletons.put(type, created);
            return created;
        }
        throw new IllegalStateException("No registration for: " + type.getName());
    }

    public <T> Optional<T> tryResolve(Class<T> type) {
        try {
            return Optional.of(resolve(type));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
    }

    public void clear() {
        singletons.clear();
        factories.clear();
    }
}
