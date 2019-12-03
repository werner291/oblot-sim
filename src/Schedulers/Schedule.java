package Schedulers;

import Algorithms.Robot;

import java.util.List;

/**
 * A schedule for oblivious robots. It contains for every robot the timestamps of its look, compute and move cycles
 */
public class Schedule {

    public double start = 0;
    public double stopTime;

    // For every robot there is an array in timestamps containing the look, compute, move cycles
    // This array for one robot has a length that is a multiple of 4
    // The timestamps are the following in repeating order:
    // start of look phase, start of compute phase, start of move phase, end of move phase.

    // the indices for the robots are the same as for their respective timestamps
    //TODO define data format here (array vs list, depending on the schedule generator and how the simulator will need to access it)
    public List<Robot> robots;
    public double[][] timestamps;

    public Schedule(List<Robot> robots, double stopTime) {
        this.robots = robots;
        this.stopTime = stopTime;
    }
}
