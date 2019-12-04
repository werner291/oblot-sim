package Schedulers;

/**
 * An event of a robot. Could be either start compute, start moving
 */
public class Event {

    /**
     * The type of the event.
     */
    public EventType type;
    /**
     * The timestamp of the event.
     */
    public double timeStamp;

    /**
     * Creates a new event
     * @param type the type of event
     * @param timeStamp the timestamp of the event
     */
    public Event(EventType type, double timeStamp) {
        this.type = type;
        this.timeStamp = timeStamp;
    }

    /**
     * The different types of events
     */
    public enum EventType {
        START_COMPUTE,
        START_MOVING,
        END_MOVING
    }
}
