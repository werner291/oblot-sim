package Algorithms;

import java.util.List;

/**
 * An abstract class that is a template for algorithms run by {@link Robot}
 */
public abstract class Algorithm {

    /**
     * Calculate a list of positions to go to, given a certain snapshot of the robots.
     * @param snapshot an iterable of all the positions of the robots at the time of the snapshot
     * @param pos the current position of this robot
     * @return A list of positions the robot wants to move to.
     */
    public abstract List<Vector> doAlgorithm(Vector[] snapshot, Vector pos);
}
