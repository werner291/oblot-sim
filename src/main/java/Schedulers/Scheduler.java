package Schedulers;


import Algorithms.Robot;

import java.util.List;

/**
 * An abstract scheduler. Is able to generate a schedule when given a list of robots.
 */
public abstract class Scheduler {

    /**
     * Gets the next event(s), given a snapshot of the robots and the current time.
     * If there is an event at timestamp t, it will not return that event, but the event(s) directly afterwards
     * @param robots the snapshot of the robots
     * @param t the current time
     * @return the next event(s), null if there is none or a list if multiple happen on the same timestamp
     */
    public abstract List<Event> getNextEvent(Robot[] robots, double t);
}
