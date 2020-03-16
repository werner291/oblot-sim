package nl.tue.oblotsim.algorithms;

import nl.tue.oblotsim.RobotPaths.LinearPath;
import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Util.Vector;

/**
 * An really simple algorithm that does not let the robots move at all. Can be used for testing
 */
public class DoNothing extends Algorithm {

    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        return new LinearPath(Vector.ZERO);
    }
}
