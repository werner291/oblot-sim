package Schedulers;

import Util.Vector;

import java.util.List;

/**
 * An event for the simulator. Will be used to store what happened in the simulator.
 * It is used whenever an event has been processed and the goal of the robot in question is known.
 */
public class CalculatedEvent {
    /**
     * A list of events which all happen at the same timestamp.
     */
    public List<Event> events;

    /**
     * The current position of the robot
     */
    public Vector[] positions;

    /**
     * The position the robot wants to go to. If the robot is not moving,
     * this will be the same as {@link CalculatedEvent#positions}
     */
    public Vector[] goals;

    public CalculatedEvent(List<Event> events, Vector[] positions, Vector[] goals) {
        if (events.isEmpty()) {
            throw new IllegalArgumentException("Events cannot be empty");
        }
        this.events = events;
        this.positions = positions;
        this.goals = goals;
    }
}
