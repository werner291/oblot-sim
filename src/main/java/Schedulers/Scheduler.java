package Schedulers;


import Simulator.Robot;

import java.util.List;

/**
 * An abstract scheduler. Is able to generate a schedule when given a list of robots.
 */
public abstract class Scheduler {

    /**
     * Return all events that are shortest after timestep t, that are strictly greater than t.
     * So it returns a list of all events e such that e.t > t and for all other events o, e.t < o.t
     *
     * Therefore, if there is an event at timestep t, it will not return that event,
     * but instead it will return the next one
     *
     * If there are multiple events for the same robot they should be in the natural order of events (START_COMPUTE, START_MOVE, END_MOVE).
     * @param robots the snapshot of the robots
     * @param t the current time
     * @return the next event(s), null if there is none or a list if multiple happen on the same timestamp
     */
    public abstract List<Event> getNextEvent(Robot[] robots, double t);

    /**
     * Add an event that the scheduler should take into account.
     * For an added event e, if the scheduler already planned a similar event e' with
     * the same robot and type, a different timestamp and no other events for that robot in between e.t and e'.t,
     * it will remove the latest of the two. So if e.t < e'.t, it will discard the event that was send. If e.t > e'.t
     * e' will be discarded.
     * Moreover, if e breaks the natural chain of events (START_COMPUTE, START_MOVE, END_MOVE), it will also be discarded.
     * @param e the event to send to the scheduler
     */
    public abstract void addEvent(Event e);
}
