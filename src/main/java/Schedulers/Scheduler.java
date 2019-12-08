package Schedulers;


import Algorithms.Robot;

import java.util.List;

/**
 * An abstract scheduler. Is able to generate a schedule when given a list of robots.
 */
public abstract class Scheduler {

    /**
     * `getNextEvent` will be called exactly once at the start of the simulation and exactly once at every subsequent time point
     * where an event occurs, in monotonically increasing time order.
     *
     * At this time, the method is to produce all events that it knows will certainly occur. Mostly, these events
     * will be ones instructing robots to take a snapshot of the enviornment, to start and to stop moving.
     *
     * All events returned by this method must:
     *  - Occur strictly in the future relative to `t`
     *  - Occur certainly. Events returned may not be cancelled. Such potential events must be provided on a later call
     *    when their occurance becomes certain.
     *  - Occur once: The scheduler should make sure not to produce duplicate events on subsequent calls.
     *
     * Gets the next event(s), given a snapshot of the robots and the current time.
     * If there is an event at timestamp t, it will not return that event, but the event(s) directly afterwards
     * @param robots The snapshot of the robots at time `t`.
     * @param t the current time (This will be strictly greater for each call.)
     * @param triggers All events that occur at time `t`
     * @return the next event(s), null if there is none or a list if multiple happen on the same timestamp
     */
    public abstract List<Event> getNextEvent(Robot[] robots, double t, Event[] triggers);
}
