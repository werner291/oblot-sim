package Algorithms;

import RobotPaths.CircularPath;
import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Util.Circle;
import Util.SmallestEnclosingCircle;
import Util.Vector;

import java.util.Arrays;

public class MoveAlongSEC extends Algorithm {
    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        Circle SEC = SmallestEnclosingCircle.makeCircle(Arrays.asList(snapshot));
        if (SEC.on(origin)) {
            Vector toCenter = SEC.c.sub(origin);
            return new CircularPath(origin, SEC.c, origin.add(toCenter).add(toCenter), false);
        } else {
            return new LinearPath(SEC.getPointOnCircle(origin));
        }
    }
}
