package Schedulers;

import Util.Vector;

/**
 * An event for the simulator. Will be used to store what happened in the simulator.
 * It is used whenever an event has been processed and the goal of the robot in question is known.
 */
public class CalculatedEvent {
    /**
     * The accompanying event
     */
    public Event e;

    /**
     * The current position of the robot
     */
    public Vector pos;

    /**
     * The position the robot wants to go to. If the robot is not moving,
     * this will be the same as {@link CalculatedEvent#pos}
     */
    public Vector goal;
}
