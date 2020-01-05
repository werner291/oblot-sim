package Algorithms;

import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Util.Vector;

import java.util.Arrays;

/**
 * Algorithm that always moves robots to the rightmost robot.
 */
public class GoToRightMost extends Algorithm {
    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        return new LinearPath(Arrays.stream(snapshot).max((a, b) -> Double.compare(a.x, b.x)).get());
    }
}
