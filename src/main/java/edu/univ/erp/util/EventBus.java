package edu.univ.erp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simple in-process event bus for real-time UI updates.
 * Supports topic-based pub/sub pattern.
 */
public class EventBus {
    private static final EventBus instance = new EventBus();
    private final Map<String, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    
    private EventBus() {
        // Singleton
    }
    
    public static EventBus getInstance() {
        return instance;
    }
    
    /**
     * Register a handler for a topic.
     * @param topic the topic name (e.g., "maintenance.changed")
     * @param handler the handler function to call when an event is posted
     */
    public void register(String topic, Consumer<Object> handler) {
        subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(handler);
    }
    
    /**
     * Unregister a handler for a topic.
     * @param topic the topic name
     * @param handler the handler to remove
     */
    public void unregister(String topic, Consumer<Object> handler) {
        List<Consumer<Object>> handlers = subscribers.get(topic);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
    
    /**
     * Post an event to a topic.
     * @param topic the topic name
     * @param payload the event payload (can be any object)
     */
    public void post(String topic, Object payload) {
        List<Consumer<Object>> handlers = subscribers.get(topic);
        if (handlers != null) {
            // Create a copy to avoid concurrent modification issues
            List<Consumer<Object>> handlersCopy = new ArrayList<>(handlers);
            for (Consumer<Object> handler : handlersCopy) {
                try {
                    handler.accept(payload);
                } catch (Exception e) {
                    // Log error but don't break other handlers
                    System.err.println("Error in event handler for topic " + topic + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Clear all subscribers (useful for testing).
     */
    public void clear() {
        subscribers.clear();
    }
}

