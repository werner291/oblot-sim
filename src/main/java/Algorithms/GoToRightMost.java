package Algorithms;

import Util.Vector;

import java.util.Arrays;

/**
 * Algorithm that always moves robots to the rightmost robot.
 */
public class GoToRightMost extends Algorithm {
    @Override
    public Vector doAlgorithm(Vector[] snapshot) {
        return Arrays.stream(snapshot).max((a, b) -> Double.compare(a.x, b.x)).get();
    }
}
