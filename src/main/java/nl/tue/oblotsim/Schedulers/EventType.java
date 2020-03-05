package nl.tue.oblotsim.Schedulers;

/**
 * The different types of events
 */
public enum EventType {
    START_COMPUTE,
    START_MOVING,
    END_MOVING;

    public static EventType next(EventType e) {
        return EventType.values()[(e.ordinal() + 1) % EventType.values().length];
    }
}