package dev.hardobfuscator.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Thread-safe publish/subscribe event bus for pipeline lifecycle notifications.
 */
public final class EventBus {

    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends ObfuscationEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends ObfuscationEvent> void publish(T event) {
        List<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null) {
            return;
        }
        for (Consumer<?> listener : eventListeners) {
            try {
                ((Consumer<T>) listener).accept(event);
            } catch (Exception e) {
                log.warn("Event listener failed for {}: {}", event.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    public void clear() {
        listeners.clear();
    }
}
