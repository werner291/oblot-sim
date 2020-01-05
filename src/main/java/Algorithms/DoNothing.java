package Algorithms;

import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Util.Vector;

/**
 * An really simple algorithm that does not let the robots move at all. Can be used for testing
 */
public class DoNothing extends Algorithm {

    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        return new LinearPath(origin);
    }
}
