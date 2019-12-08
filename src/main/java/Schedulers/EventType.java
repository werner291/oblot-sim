package Schedulers;

/**
 * The different types of events
 */
public enum EventType {
    LOOK_COMPUTE, // A robot's algorithm will be evaluated at this time
    START_MOVING,
    END_MOVING;

    public static EventType next(EventType e) {
        return EventType.values()[(e.ordinal() + 1) % EventType.values().length];
    }
}