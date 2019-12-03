package Schedulers;


import Algorithms.Robot;

import java.util.List;
import java.util.Random;

/**
 * An abstract scheduler. Is able to generate a schedule when given a list of robots.
 */
public abstract class Scheduler {

    // the rng for random generation of schedules
    public Random random;

    public Scheduler(long seed) {
        this.random = new Random(seed);
    }

    public abstract Schedule generate(Iterable<Robot> robots);
}
