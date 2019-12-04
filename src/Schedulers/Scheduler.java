package Schedulers;


import Algorithms.Robot;

/**
 * An abstract scheduler. Is able to generate a schedule when given a list of robots.
 */
public abstract class Scheduler {

    /**
     * Gets the next event, given a snapshot of the robots and the current time
     * @param robots the snapshot of the robots
     * @param t the current time
     * @return the next event or null if there is none
     */
    public abstract Event getNextEvent(Iterable<Robot> robots, double t);
}
