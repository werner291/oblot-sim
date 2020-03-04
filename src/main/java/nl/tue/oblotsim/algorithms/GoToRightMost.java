package nl.tue.oblotsim.Algorithms;

import nl.tue.oblotsim.RobotPaths.LinearPath;
import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Util.Vector;

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
