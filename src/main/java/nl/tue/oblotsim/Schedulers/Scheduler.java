package nl.tue.oblotsim.Schedulers;


import nl.tue.oblotsim.Simulator.Robot;

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
     * but instead it will return the next one.
     *
     * If there are multiple events for the same robot they should be in the natural order of events (START_COMPUTE, START_MOVE, END_MOVE).
     *
     * Warning: No events with time 0 can occur! Any event returned must have time at least t > 0.
     *
     * @param robots the snapshot of the robots
     * @param t the current time
     * @return the next event(s), null if there is none or a list if multiple happen on the same timestamp
     */
    public abstract List<Event> getNextEvent(List<Robot> robots, double t);

}
