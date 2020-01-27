package Schedulers;

import Simulator.Robot;

/**
 * An event of a robot. Could be either start compute, start moving
 */
public class Event {

    private EventType type;
    private double t;

    private int targetId;

    /**
     * Creates a new event
     * @param type the type of event
     * @param t the timestamp of the event
     * @param r the {@link Robot} this event belongs to
     */
    public Event(EventType type, double t, int targetRobotId) {
        this.type = type;
        this.t = t;
        this.targetId = targetRobotId;
    }

    @Override
    public String toString() {
        return String.format("Event: %s, %d, %f", type, targetId, t);
    }

    /**
     * The type of the event.
     */
    public EventType getType() {
        return type;
    }

    /**
     * The timestamp of the event.
     */
    public double getT() {
        return t;
    }

    /**
     * The {@link Robot} this event belongs to.
     */
    public int getTargetId() {
        return targetId;
    }
}
