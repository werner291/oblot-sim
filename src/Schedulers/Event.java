package Schedulers;

import Algorithms.Robot;

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
     * The {@link Robot} this event belongs to.
     */
    public Robot r;

    /**
     * Creates a new event
     * @param type the type of event
     * @param timeStamp the timestamp of the event
     * @param r the {@link Robot} this event belongs to
     */
    public Event(EventType type, double timeStamp, Robot r) {
        this.type = type;
        this.timeStamp = timeStamp;
        this.r = r;
    }

    @Override
    public String toString() {
        return String.format("Event: %s, %s, %f", type, r, timeStamp);
    }
}
