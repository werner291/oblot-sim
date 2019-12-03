package Algorithms;

import java.util.List;

/**
 * A class containing one oblivious robot
 */
public class Robot {

    Algorithm algo;
    Vector position;

    /**
     * Calculate where the robot wants to go
     * @param snapshot a snapshot of the positions of the robots at a certain timestamp
     * @return A list of positions the robot wants to go to.
     */
    public List<Vector> calculate(Iterable<Vector> snapshot) {
        return algo.doAlgorithm(snapshot);
    }
}
