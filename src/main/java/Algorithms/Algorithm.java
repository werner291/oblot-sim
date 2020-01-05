package Algorithms;

import RobotPaths.RobotPath;
import Simulator.Robot;
import Util.Vector;

/**
 * An abstract class that is a template for algorithms run by {@link Robot}
 */
public abstract class Algorithm {

    Vector origin = new Vector(0, 0);

    /**
     * Calculate a list of positions to go to, given a certain snapshot of the robots.
     * The current position of the robot is always at the origin, as this is in the local coordinate system
     * @param snapshot an iterable of all the positions of the robots at the time of the snapshot in the local coordinate
     *                 system of the robot
     * @return The position in local space the robot wants to move to
     */
    public abstract RobotPath doAlgorithm(Vector[] snapshot);
}
