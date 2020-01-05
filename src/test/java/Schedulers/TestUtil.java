package Schedulers;

import Algorithms.Algorithm;
import PositionTransformations.RotationTransformation;
import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Simulator.Robot;
import Util.Vector;

import java.util.Arrays;
import java.util.stream.Stream;

public class TestUtil {
    /*
     * This algorithm is very simple: Just direct the robot to go to the center of gravity of all robot positions.
     */
    public static final Algorithm GO_TO_COG = new Algorithm() {
        @Override
        public RobotPath doAlgorithm(Vector[] snapshot) {
            //noinspection OptionalGetWithoutIsPresent (We know the robot itself is present, so the COG is defined)
            return new LinearPath(Arrays.stream(snapshot).reduce((vA, vB) -> vA.add(vB)).get().mult(1 / (double) snapshot.length));
        }
    };

    /*
     * Algorithm that causes the robot to not move.
     */
    public static final Algorithm DO_NOTHING = new Algorithm() {
        @Override
        public RobotPath doAlgorithm(Vector[] snapshot) {
            return new LinearPath(new Vector(0.0,0.0));
        }
    };

    public static Robot[] generateRobotCloud(Algorithm algo, double edgeLen, int n) {
        Robot[] robots = new Robot[n];
        for (int i = 0; i < robots.length; i++) {
            robots[i] = new Robot(i, algo,
                                new Vector(2.0 * edgeLen * (Math.random() - 0.5),
                                2.0 * edgeLen * (Math.random() - 0.5)),
                                new RotationTransformation());
        }
        return robots;
    }
}
